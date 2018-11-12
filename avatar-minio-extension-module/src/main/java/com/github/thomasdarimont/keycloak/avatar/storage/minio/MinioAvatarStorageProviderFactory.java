package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProvider;
import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProviderFactory;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
public class MinioAvatarStorageProviderFactory implements AvatarStorageProviderFactory {

    @Override
    public AvatarStorageProvider create(KeycloakSession session) {

        MinioConfig config = new MinioConfig("http://172.17.0.2:9000", "AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        return new MinioAvatarStorageProvider(config);
    }

    @Override
    public void init(Config.Scope config) {
        // TODO pull config from settings
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public String getId() {
        return "avatar-storage-minio";
    }
}
