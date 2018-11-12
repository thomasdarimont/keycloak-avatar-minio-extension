package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import com.github.thomasdarimont.keycloak.avatar.storage.AvatarStorageProvider;
import lombok.extern.jbosslog.JBossLog;

import java.io.InputStream;

@JBossLog
public class MinioAvatarStorageProvider implements AvatarStorageProvider {

    private final MinioTemplate minioTemplate;

    public MinioAvatarStorageProvider(MinioConfig minioConfig) {
        this.minioTemplate = new MinioTemplate(minioConfig);
    }

    @Override
    public void saveAvatarImage(String realmName, String userId, InputStream input) {

        String bucketName = minioTemplate.getBucketName(realmName);
        minioTemplate.ensureBucketExists(bucketName);

        minioTemplate.execute(minioClient -> {
            minioClient.putObject(bucketName, userId, input, "image/png");
            return null;
        });
    }

    @Override
    public InputStream loadAvatarImage(String realmName, String userId) {

        String bucketName = minioTemplate.getBucketName(realmName);

        return minioTemplate.execute(minioClient -> minioClient.getObject(bucketName, userId));
    }

    @Override
    public void close() {
        // NOOP
    }


}
