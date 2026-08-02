package com.tnyx.di

import com.tnyx.data.onboarding.DataStoreOnboardingRepository
import com.tnyx.data.onboarding.ResumeManager
import com.tnyx.data.onboarding.SupabaseOnboardingCompletionSyncRepository
import com.tnyx.features.onboarding.domain.repository.OnboardingCompletionSyncRepository
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import com.tnyx.features.onboarding.domain.resume.ResumeManager as OnboardingResumeManager
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

    @Binds
    @Singleton
    abstract fun bindOnboardingCompletionSyncRepository(
        repository: SupabaseOnboardingCompletionSyncRepository,
    ): OnboardingCompletionSyncRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingResumeManager(
        resumeManager: ResumeManager,
    ): OnboardingResumeManager
}
