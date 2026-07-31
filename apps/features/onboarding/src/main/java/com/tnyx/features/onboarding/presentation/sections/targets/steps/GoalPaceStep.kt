package com.tnyx.features.onboarding.presentation.sections.targets.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun GoalPaceStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "How fast should Tio push your goal pace?",
            description = "This helps us keep the plan realistic for your current routine instead of forcing one generic target.",
        )
        GoalPaceOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose a pace to continue")
        }
    }
}

private enum class GoalPaceOption(
    val id: String,
    val label: String,
    val description: String,
) {
    Relaxed(
        id = "relaxed",
        label = "Relaxed",
        description = "Build consistency first with lower pressure and easier day-to-day targets.",
    ),
    Steady(
        id = "steady",
        label = "Steady",
        description = "Use a balanced pace that still expects regular effort and measurable progress.",
    ),
    Ambitious(
        id = "ambitious",
        label = "Ambitious",
        description = "Aim for a stronger push if you want faster progress and can sustain the routine.",
    ),
}
