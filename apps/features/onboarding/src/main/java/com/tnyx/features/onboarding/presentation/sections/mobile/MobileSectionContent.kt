package com.tnyx.features.onboarding.presentation.sections.mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.mobile.steps.MobileNumberStep

@Composable
internal fun MobileSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.MobileNumber -> MobileNumberStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}
