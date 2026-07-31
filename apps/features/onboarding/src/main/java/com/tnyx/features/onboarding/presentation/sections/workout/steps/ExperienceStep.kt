package com.tnyx.features.onboarding.presentation.sections.workout.steps

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
internal fun ExperienceStep(
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
            title = "How much workout experience do you have?",
            description = "This helps Tio start at the right difficulty and progression pace.",
        )
        ExperienceOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                badge = option.badge,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one experience level to continue")
        }
    }
}

private enum class ExperienceOption(
    val id: String,
    val label: String,
    val badge: String,
    val description: String,
) {
    Fresh(
        id = "fresh",
        label = "Fresh",
        badge = "Just starting",
        description = "You are new to structured workouts and want a gentle on-ramp.",
    ),
    Beginner(
        id = "beginner",
        label = "Beginner",
        badge = "Some basics",
        description = "You know a few movements and want steady fundamentals.",
    ),
    Intermediate(
        id = "intermediate",
        label = "Intermediate",
        badge = "Regular training",
        description = "You already train with consistency and can handle more structure.",
    ),
    Advanced(
        id = "advanced",
        label = "Advanced",
        badge = "High familiarity",
        description = "You are comfortable with structured training and progressive load.",
    ),
}
