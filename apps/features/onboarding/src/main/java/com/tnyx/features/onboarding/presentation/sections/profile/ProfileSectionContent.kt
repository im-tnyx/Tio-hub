package com.tnyx.features.onboarding.presentation.sections.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.profile.steps.DateOfBirthStep
import com.tnyx.features.onboarding.presentation.sections.profile.steps.GenderStep
import com.tnyx.features.onboarding.presentation.sections.profile.steps.NameStep

@Composable
internal fun ProfileSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.ProfileName -> NameStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.ProfileGender -> GenderStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.ProfileDateOfBirth -> DateOfBirthStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}
