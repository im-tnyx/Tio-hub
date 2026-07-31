package com.tnyx.di

import com.tnyx.features.onboarding.domain.resume.ResumeManager
import com.tnyx.features.onboarding.domain.usecase.BuildFlowUseCase
import com.tnyx.features.onboarding.domain.usecase.RestoreFlowUseCase
import com.tnyx.features.onboarding.domain.usecase.ValidateOnboardingAnswerUseCase
import com.tnyx.features.onboarding.domain.validator.StepValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OnboardingUseCaseModule {
    @Provides
    fun provideBuildFlowUseCase(): BuildFlowUseCase = BuildFlowUseCase()

    @Provides
    fun provideRestoreFlowUseCase(
        resumeManager: ResumeManager,
    ): RestoreFlowUseCase = RestoreFlowUseCase(resumeManager)

    @Provides
    fun provideStepValidator(
        validateOnboardingAnswer: ValidateOnboardingAnswerUseCase,
    ): StepValidator = StepValidator(validateOnboardingAnswer)
}
