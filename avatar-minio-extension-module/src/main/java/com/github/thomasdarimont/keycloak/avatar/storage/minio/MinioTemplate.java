package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@RequiredArgsConstructor
public class MinioTemplate {

    private final MinioConfig minioConfig;

    public <T> T execute(MinioCallback<T> callback) {

        try {
            MinioClient minioClient = new MinioClient(minioConfig.getServerUrl(), minioConfig.getAccessKey(), minioConfig.getSecretKey());
            return callback.doInMinio(minioClient);
        } catch (Exception mex) {
            throw new RuntimeException(mex);
        }
    }

    public void ensureBucketExists(String bucketName) {

        execute(minioClient -> {

            boolean exists = minioClient.bucketExists(bucketName);
            if (exists) {
                log.debugf("Bucket: %s already exists", bucketName);
            } else {
                minioClient.makeBucket(bucketName);
            }

            return null;
        });
    }


    public String getBucketName(String realmName) {
        return realmName + minioConfig.getDefaultBucketSuffix();
    }

}