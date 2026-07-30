package com.tnyx.di

import com.tnyx.features.onboarding.domain.analytics.OnboardingAnalyticsLogger
import com.tnyx.features.onboarding.domain.analytics.OnboardingAnalyticsTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingAnalyticsModule {
    @Provides
    @Singleton
    fun provideOnboardingAnalyticsLogger(): OnboardingAnalyticsLogger {
        return OnboardingAnalyticsLogger()
    }

    @Provides
    @Singleton
    fun provideOnboardingAnalyticsTracker(
        logger: OnboardingAnalyticsLogger,
    ): OnboardingAnalyticsTracker {
        return OnboardingAnalyticsTracker(logger)
    }
}
