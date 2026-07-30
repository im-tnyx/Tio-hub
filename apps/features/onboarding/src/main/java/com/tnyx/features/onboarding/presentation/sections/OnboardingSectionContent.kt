package com.tnyx.features.onboarding.presentation.sections

import androidx.compose.runtime.Composable
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.OnboardingAction
import com.tnyx.features.onboarding.presentation.OnboardingValidationError
import com.tnyx.features.onboarding.presentation.renderer.SectionRenderer

@Composable
internal fun OnboardingSectionContent(
    position: OnboardingPosition,
    currentAnswer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    validationError: OnboardingValidationError?,
    onAction: (OnboardingAction) -> Unit,
) {
    SectionRenderer(
        position = position,
        currentAnswer = currentAnswer,
        draftAnswers = draftAnswers,
        validationError = validationError,
        onAction = onAction,
    )
}
