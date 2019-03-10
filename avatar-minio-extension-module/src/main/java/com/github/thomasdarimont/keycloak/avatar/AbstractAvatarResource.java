package com.github.thomasdarimont.keycloak.avatar;

import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.keycloak.models.KeycloakSession;

public abstract class AbstractAvatarResource {
    protected static final String AVATAR_IMAGE_PARAMETER = "image";

    protected KeycloakSession session;

    public AbstractAvatarResource(KeycloakSession session) {
        this.session = session;
    }

    public AvatarStorageProvider getAvatarStorageProvider() {
        return lookupAvatarStorageProvider(session);
    }

    protected AvatarStorageProvider lookupAvatarStorageProvider(KeycloakSession keycloakSession) {
        return keycloakSession.getProvider(AvatarStorageProvider.class);
    }

    protected Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    protected void saveUserImage(String realmName, String userId, InputStream imageInputStream) {
        getAvatarStorageProvider().saveAvatarImage(realmName, userId, imageInputStream);
    }

    protected StreamingOutput fetchUserImage(String realmId, String userId) {
        return output -> copyStream(getAvatarStorageProvider().loadAvatarImage(realmId, userId), output);
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
