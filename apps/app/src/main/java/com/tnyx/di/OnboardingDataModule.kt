package com.tnyx.di

import com.tnyx.data.onboarding.DataStoreOnboardingRepository
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingDataModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        repository: DataStoreOnboardingRepository,
    ): OnboardingRepository
}
