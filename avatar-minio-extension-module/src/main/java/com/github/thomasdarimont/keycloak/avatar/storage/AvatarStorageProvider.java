package com.github.thomasdarimont.keycloak.avatar.storage;

import org.keycloak.provider.Provider;

import java.io.InputStream;

public interface AvatarStorageProvider extends Provider {

    void saveAvatarImage(String realmName, String userId, InputStream input);

    InputStream loadAvatarImage(String realmId, String userId);
}
