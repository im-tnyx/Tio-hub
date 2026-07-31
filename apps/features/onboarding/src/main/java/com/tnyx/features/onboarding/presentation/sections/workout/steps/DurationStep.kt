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
internal fun DurationStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedMinutes = (answer as? OnboardingAnswer.Decimal)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "How long can most workouts be?",
            description = "Pick a session length that feels realistic on your normal schedule.",
        )
        WorkoutDurationOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedMinutes == option.minutes,
                onClick = { onAnswerChanged(OnboardingAnswer.Decimal(option.minutes)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one workout duration to continue")
        }
    }
}

private enum class WorkoutDurationOption(
    val minutes: Double,
    val label: String,
    val description: String,
) {
    Thirty(
        minutes = 30.0,
        label = "30 minutes",
        description = "Short, focused sessions that fit busy days well.",
    ),
    FortyFive(
        minutes = 45.0,
        label = "45 minutes",
        description = "A balanced choice for most strength or conditioning plans.",
    ),
    Sixty(
        minutes = 60.0,
        label = "60 minutes",
        description = "Enough time for a fuller workout with warmup and accessories.",
    ),
    Ninety(
        minutes = 90.0,
        label = "90 minutes",
        description = "Best when you like more volume or longer training blocks.",
    ),
    OneTwenty(
        minutes = 120.0,
        label = "120 minutes",
        description = "For long gym sessions with high training availability.",
    ),
}
