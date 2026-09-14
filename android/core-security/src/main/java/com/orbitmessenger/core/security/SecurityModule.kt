package com.orbitmessenger.core.security

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyStoreManager {
        return KeyStoreManager()
    }

    @Provides
    @Singleton
    fun provideCryptoManager(keyStoreManager: KeyStoreManager): CryptoManager {
        return CryptoManager(keyStoreManager)
    }
}
