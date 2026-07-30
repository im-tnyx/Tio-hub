package com.tnyx.features.onboarding.presentation.sections.bodygoal.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer

@Composable
internal fun CurrentWeightStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BodyGoalDecimalInputStep(
        title = "What is your current weight?",
        description = "Your starting weight helps Tio calculate practical targets and progress.",
        label = "Current weight",
        placeholder = "72.5",
        helperMessage = "You can refine this later from Profile or Progress",
        unit = "kg",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a valid current weight in kg",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}
