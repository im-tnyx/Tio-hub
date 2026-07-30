package com.tnyx.features.onboarding.presentation.sections.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.source.steps.SourceChannelStep
import com.tnyx.features.onboarding.presentation.sections.source.steps.SourceReasonStep

@Composable
internal fun SourceSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.SourceChannel -> {
            SourceChannelStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }

        OnboardingStepIds.SourceReason -> {
            SourceReasonStep(
                answer = answer,
                showValidationError = showValidationError,
                onAnswerChanged = onAnswerChanged,
                modifier = modifier,
            )
        }
    }
}
