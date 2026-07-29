package com.tnyx.di

import com.tnyx.data.auth.DataStoreAuthSessionStore
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.auth.domain.repository.MutableAuthSessionStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthSessionDataModule {
    @Binds
    @Singleton
    abstract fun bindMutableAuthSessionStore(
        store: DataStoreAuthSessionStore,
    ): MutableAuthSessionStore

    companion object {
        @Provides
        fun provideAuthSessionProvider(
            store: MutableAuthSessionStore,
        ): AuthSessionProvider = store
    }
}
