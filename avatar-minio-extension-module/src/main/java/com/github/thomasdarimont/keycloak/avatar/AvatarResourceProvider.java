package com.github.thomasdarimont.keycloak.avatar;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class AvatarResourceProvider implements RealmResourceProvider {

    private final KeycloakSession keycloakSession;

    public AvatarResourceProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }


    @Override
    public Object getResource() {
        return new AvatarResource(keycloakSession);
    }

    @Override
    public void close() {
        // NOOP
    }
}
