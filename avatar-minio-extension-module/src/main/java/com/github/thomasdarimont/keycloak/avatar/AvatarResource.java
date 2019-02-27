package com.github.thomasdarimont.keycloak.avatar;

import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProvider;
import com.github.thomasdarimont.keycloak.avatar.storage.minio.MinioAvatarStorageProviderFactory;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.RealmsResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

public class AvatarResource {
    private static final Logger log = Logger.getLogger(AvatarResource.class);

    private static final String AVATAR_IMAGE_PARAMETER = "image";
    public static final String STATE_CHECKER_ATTRIBUTE = "state_checker";
    public static final String STATE_CHECKER_PARAMETER = "stateChecker";

    private final KeycloakSession keycloakSession;

    private final AuthenticationManager.AuthResult auth;

    private final AvatarStorageProvider avatarStorageProvider;

    private final AdminPermissionEvaluator adminPermissionEvaluator;

    public AvatarResource(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
        this.auth = resolveAuthentication(keycloakSession);
        this.avatarStorageProvider = lookupAvatarStorageProvider(keycloakSession);
        RealmModel realm = keycloakSession.getContext().getRealm();
        AdminAuth adminAuth = new AdminAuth(realm, auth.getToken(), auth.getUser(), realm.getClientByClientId("realm-management"));
        this.adminPermissionEvaluator = AdminPermissions.evaluator(keycloakSession, keycloakSession.getContext().getRealm(), adminAuth);
    }

    private AuthenticationManager.AuthResult resolveAuthentication(KeycloakSession keycloakSession) {

        AppAuthManager appAuthManager = new AppAuthManager();
        RealmModel realm = keycloakSession.getContext().getRealm();

        AuthenticationManager.AuthResult authResult  = appAuthManager.authenticateBearerToken(keycloakSession, realm);
        if (authResult != null) {
            return authResult;
        }

        KeycloakContext context = keycloakSession.getContext();
        MultivaluedMap<String, String> queryParameters = context.getUri().getQueryParameters(true);
        if (queryParameters.containsKey("access_token")) {
            String accessToken = queryParameters.getFirst("access_token");
            authResult = appAuthManager.authenticateBearerToken(accessToken, keycloakSession, context.getRealm(), context.getUri(), context.getConnection(), context.getRequestHeaders());
            if (authResult != null) {
                return authResult;
            }
        }

        authResult = appAuthManager.authenticateIdentityCookie(keycloakSession, realm);
        if (authResult != null) {
            return authResult;
        }

        return null;
    }

    private AvatarStorageProvider lookupAvatarStorageProvider(KeycloakSession keycloakSession) {

        // TODO deploy AvatarStorageProvider SPI in Keycloak
        // return keycloakSession.getProvider(AvatarStorageProvider.class);
        return new MinioAvatarStorageProviderFactory().create(keycloakSession);
    }

    @GET
    @Path("/avatar/{user_id}")
    @Produces({"image/png", "image/jpeg", "image/gif"})
    public Response downloadUserAvatarImage(@PathParam("user_id") String userId) {
        try {
            if (auth == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            canViewUsers();

            RealmModel realm = auth.getSession().getRealm();

            return Response.ok(fetchUserImage(realm.getName(), userId)).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("error getting user avatar", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @NoCache
    @Path("/avatar/{user_id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadUserAvatarImage(@PathParam("user_id") String userId, MultipartFormDataInput input) {
        try {
            if (auth == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            canManageUsers();

            RealmModel realm = auth.getSession().getRealm();
            String realmName = realm.getName();

            InputStream imageInputStream = input.getFormDataPart(AVATAR_IMAGE_PARAMETER, InputStream.class, null);
            saveUserImage(realmName, userId, imageInputStream);

        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("error saving user avatar", e);
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/avatar")
    @Produces({"image/png", "image/jpeg", "image/gif"})
    public Response downloadCurrentUserAvatarImage() {

        if (auth == null) {
            return badRequest();
        }

        String realmName = auth.getSession().getRealm().getName();
        String userId = auth.getUser().getId();

        return Response.ok(fetchUserImage(realmName, userId)).build();
    }

    @POST
    @NoCache
    @Path("/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCurrentUserAvatarImage(MultipartFormDataInput input, @Context UriInfo uriInfo) {

        if (auth == null) {
            return badRequest();
        }

        if (!isValidStateChecker(input)) {
            return badRequest();
        }

        try {

            InputStream imageInputStream = input.getFormDataPart(AVATAR_IMAGE_PARAMETER, InputStream.class, null);

            String realmName = auth.getSession().getRealm().getName();
            String userId = auth.getUser().getId();

            saveUserImage(realmName, userId, imageInputStream);

            if (uriInfo.getQueryParameters().containsKey("account")) {
                return Response.seeOther(RealmsResource.accountUrl(keycloakSession.getContext().getUri().getBaseUriBuilder()).build(realmName)).build();
            }


            return Response.ok().build();

        } catch (Exception ex) {
            return Response.serverError().build();
        }
    }

    private boolean isValidStateChecker(MultipartFormDataInput input) {

        try {
            String actualStateChecker = input.getFormDataPart(STATE_CHECKER_PARAMETER, String.class, null);
            String requiredStateChecker = (String) keycloakSession.getAttribute(STATE_CHECKER_ATTRIBUTE);

            return actualStateChecker != null && requiredStateChecker.equals(actualStateChecker);
        } catch (Exception ex) {
            return false;
        }
    }

    private Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private void saveUserImage(String realmName, String userId, InputStream imageInputStream) {
        avatarStorageProvider.saveAvatarImage(realmName, userId, imageInputStream);
    }

    private StreamingOutput fetchUserImage(String realmId, String userId) {
        return output -> copyStream(avatarStorageProvider.loadAvatarImage(realmId, userId), output);
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[16384];

        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        out.flush();
    }

    private void canViewUsers() {
        if (!adminPermissionEvaluator.users().canView()) {
            log.info("user does not have permission to view users");
            throw new ForbiddenException("user does not have permission to view users");
        }
    }

    private void canManageUsers() {
        if (!adminPermissionEvaluator.users().canManage()) {
            log.info("user does not have permission to manage users");
            throw new ForbiddenException("user does not have manage to view users");
        }
    }
}
