package com.tnyx.di

import com.tnyx.BuildConfig
import com.tnyx.features.auth.domain.model.DemoAccountConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthConfigModule {
    @Provides
    @Singleton
    fun provideDemoAccountConfig(): DemoAccountConfig {
        return DemoAccountConfig(
            email = BuildConfig.DEMO_EMAIL,
            password = BuildConfig.DEMO_PASSWORD,
        )
    }
}
