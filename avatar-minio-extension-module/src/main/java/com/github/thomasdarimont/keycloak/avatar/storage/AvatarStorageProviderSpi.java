package com.github.thomasdarimont.keycloak.avatar.storage;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class AvatarStorageProviderSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "avatar-storage";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AvatarStorageProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AvatarStorageProviderFactory.class;
    }
}
