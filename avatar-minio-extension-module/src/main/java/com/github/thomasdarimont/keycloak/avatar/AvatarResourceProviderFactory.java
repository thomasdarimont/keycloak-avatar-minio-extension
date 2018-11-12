package com.github.thomasdarimont.keycloak.avatar;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

import static org.keycloak.Config.Scope;

public class AvatarResourceProviderFactory implements RealmResourceProviderFactory {

    private AvatarResourceProvider avatarResourceProvider;

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        if (avatarResourceProvider == null) {
            avatarResourceProvider = new AvatarResourceProvider(keycloakSession);
        }
        return avatarResourceProvider;
    }

    @Override
    public void init(Scope scope) {
        
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public String getId() {
        return "avatar-provider";
    }
}
