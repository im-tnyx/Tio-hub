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
internal fun GymAccessStep(
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
            title = "What setup do you actually have access to right now?",
            description = "This is your real-world access baseline. Your weekly plan preference can still be different on the next step.",
        )
        WorkoutGymAccessOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one access setup to continue")
        }
    }
}

private enum class WorkoutGymAccessOption(
    val id: String,
    val label: String,
    val description: String,
) {
    Gym(
        id = "gym",
        label = "Gym only",
        description = "You have regular gym access and do not need home equipment to shape the plan.",
    ),
    Home(
        id = "home",
        label = "Home only",
        description = "Your plan needs to work with home access and whatever gear you already have.",
    ),
    Both(
        id = "both",
        label = "Both",
        description = "You move between home and gym, so the plan should stay flexible across both setups.",
    ),
}
