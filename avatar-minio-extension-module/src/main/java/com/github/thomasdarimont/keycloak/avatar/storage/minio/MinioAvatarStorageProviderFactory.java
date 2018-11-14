package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProvider;
import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProviderFactory;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
public class MinioAvatarStorageProviderFactory implements AvatarStorageProviderFactory {

    //TODO remove default settings
    private static final String DEFAULT_SERVER_URL = "http://172.17.0.2:9000";
    private static final String DEFAULT_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String DEFAULT_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    private MinioConfig minioConfig = new MinioConfig(DEFAULT_SERVER_URL, DEFAULT_ACCESS_KEY, DEFAULT_SECRET_KEY);

    @Override
    public AvatarStorageProvider create(KeycloakSession session) {
        return new MinioAvatarStorageProvider(minioConfig);
    }

    @Override
    public void init(Config.Scope config) {

        String serverUrl = config.get("server-url", DEFAULT_SERVER_URL);
        String accessKey = config.get("access-key", DEFAULT_ACCESS_KEY);
        String secretKey = config.get("secret-key", DEFAULT_SECRET_KEY);

        this.minioConfig = new MinioConfig(serverUrl, accessKey, secretKey);
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
