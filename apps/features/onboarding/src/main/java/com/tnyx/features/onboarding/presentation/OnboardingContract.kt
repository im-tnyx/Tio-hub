package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingRouteContext
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingStepId

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val position: OnboardingPosition? = null,
    val currentAnswer: OnboardingAnswer? = null,
    val draftAnswers: Map<OnboardingStepId, OnboardingAnswer> = emptyMap(),
    val completionStage: OnboardingCompletionStage? = null,
    val completedFraction: Float = 0f,
    val sectionNumber: Int = 0,
    val sectionCount: Int = 0,
    val stepNumber: Int = 0,
    val totalSteps: Int = 0,
    val canContinue: Boolean = false,
    val canSkipSection: Boolean = false,
    val isLastStep: Boolean = false,
    val validationError: OnboardingValidationError? = null,
    val hasPersistenceError: Boolean = false,
)

enum class OnboardingCompletionStage {
    SettingUp,
    Ready,
}

enum class OnboardingValidationError {
    RequiredAnswerInvalid,
}

sealed interface OnboardingAction {
    data class Init(
        val initialRouteContext: OnboardingRouteContext = OnboardingRouteContext(),
    ) : OnboardingAction
    data object Retry : OnboardingAction
    data object BackClicked : OnboardingAction
    data object ContinueClicked : OnboardingAction
    data object SkipSectionClicked : OnboardingAction
    data class AnswerChanged(val answer: OnboardingAnswer?) : OnboardingAction
}

sealed interface OnboardingEffect {
    data object Exit : OnboardingEffect
    data object Completed : OnboardingEffect
}
