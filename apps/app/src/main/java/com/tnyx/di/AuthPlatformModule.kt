package com.tnyx.di

import com.tnyx.auth.SupabaseExternalAuthGateway
import com.tnyx.features.auth.domain.repository.ExternalAuthGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthPlatformModule {
    @Binds
    @Singleton
    abstract fun bindExternalAuthGateway(
        gateway: SupabaseExternalAuthGateway,
    ): ExternalAuthGateway
}
