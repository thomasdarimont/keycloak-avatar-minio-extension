package com.github.thomasdarimont.keycloak.avatar;

import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProvider;
import com.github.thomasdarimont.keycloak.avatar.storage.minio.MinioAvatarStorageProviderFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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

public class AvatarResource {

    private static final String AVATAR_IMAGE_PARAMETER = "image";

    private final KeycloakSession keycloakSession;

    private final AuthenticationManager.AuthResult auth;

    private final AvatarStorageProvider avatarStorageProvider;

    public AvatarResource(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
        this.auth = resolveAuthentication(keycloakSession);
        this.avatarStorageProvider = lookupAvatarStorageProvider(keycloakSession);
    }

    private AuthenticationManager.AuthResult resolveAuthentication(KeycloakSession keycloakSession) {

        AppAuthManager appAuthManager = new AppAuthManager();
        RealmModel realm = keycloakSession.getContext().getRealm();

        AuthenticationManager.AuthResult authResult = appAuthManager.authenticateIdentityCookie(keycloakSession, realm);
        if (authResult != null) {
            return authResult;
        }

        authResult = appAuthManager.authenticateBearerToken(keycloakSession, realm);
        return authResult;
    }

    private AvatarStorageProvider lookupAvatarStorageProvider(KeycloakSession keycloakSession) {

        // TODO deploy AvatarStorageProvider SPI in Keycloak
        // return keycloakSession.getProvider(AvatarStorageProvider.class);
        return new MinioAvatarStorageProviderFactory().create(keycloakSession);
    }

    @GET
    @Path("/avatar")
    @Produces({"image/png"})
    public Response downloadAvatarImage() {

        if (auth == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String realmName = auth.getSession().getRealm().getName();
        String userId = auth.getUser().getId();

        return Response.ok(fetchUserImage(realmName, userId)).build();
    }

    @POST
    @Path("/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadAvatarImage(MultipartFormDataInput input, @Context UriInfo uriInfo) {

        if (auth == null) {
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
            return badRequest();
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
}
