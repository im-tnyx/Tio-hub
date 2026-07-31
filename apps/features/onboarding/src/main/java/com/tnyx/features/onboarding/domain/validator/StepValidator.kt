package com.tnyx.features.onboarding.domain.validator

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingStepDefinition
import com.tnyx.features.onboarding.domain.usecase.ValidateOnboardingAnswerUseCase
import com.tnyx.features.onboarding.presentation.OnboardingUiState

class StepValidator(
    private val validateOnboardingAnswer: ValidateOnboardingAnswerUseCase,
) {
    fun isStepValid(
        stepDefinition: OnboardingStepDefinition,
        answer: OnboardingAnswer?,
    ): Boolean {
        return !stepDefinition.isRequired || validateOnboardingAnswer(stepDefinition.id, answer)
    }

    fun isStepValid(state: OnboardingUiState): Boolean = state.canContinue

    fun buttonText(state: OnboardingUiState): String {
        return when {
            state.isLastStep -> "Finish"
            else -> "Continue"
        }
    }

    fun shouldShowButton(state: OnboardingUiState): Boolean = state.position != null

    fun shouldShowBackButton(position: OnboardingPosition?): Boolean {
        return position?.stepId != OnboardingStepIds.IntroWelcome
    }

    fun shouldShowProgressBar(position: OnboardingPosition?): Boolean {
        return position?.stepId != OnboardingStepIds.IntroWelcome
    }
}
