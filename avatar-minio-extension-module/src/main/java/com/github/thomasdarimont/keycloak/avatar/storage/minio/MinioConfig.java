package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import lombok.Data;

@Data
public class MinioConfig {

    private final String serverUrl;

    private final String accessKey;

    private final String secretKey;

    private String defaultBucketSuffix = "-avatars";
}
