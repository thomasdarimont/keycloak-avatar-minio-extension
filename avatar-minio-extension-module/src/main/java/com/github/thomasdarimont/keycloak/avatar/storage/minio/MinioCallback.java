package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import io.minio.MinioClient;

public interface MinioCallback<T> {

    T doInMinio(MinioClient minioClient) throws Exception;

}