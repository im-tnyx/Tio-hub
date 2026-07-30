package com.tnyx.features.onboarding.presentation.sections.targets.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.sections.bodygoal.steps.BodyGoalDecimalInputStep

@Composable
internal fun StepsTargetStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BodyGoalDecimalInputStep(
        title = "What daily step target feels realistic?",
        description = "We start with a practical movement target that can support your body goal without feeling extreme.",
        label = "Daily steps",
        placeholder = "8000",
        helperMessage = "You can tune this later from targets or settings",
        unit = "steps",
        answer = answer,
        showValidationError = showValidationError,
        validationMessage = "Enter a whole-number target between 2000 and 30000 steps",
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}
