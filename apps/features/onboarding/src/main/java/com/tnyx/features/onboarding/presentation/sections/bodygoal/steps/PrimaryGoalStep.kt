package com.tnyx.features.onboarding.presentation.sections.bodygoal.steps

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
internal fun PrimaryGoalStep(
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
            title = "What is your main goal right now?",
            description = "Choose one primary direction so Tio can shape your targets around it.",
        )
        PrimaryGoalOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one goal to continue")
        }
    }
}

private enum class PrimaryGoalOption(
    val id: String,
    val label: String,
    val description: String,
) {
    BuildMuscle(
        id = "build_muscle",
        label = "Build muscle",
        description = "Prioritize muscle gain and a stronger body composition.",
    ),
    LoseWeight(
        id = "lose_weight",
        label = "Lose weight",
        description = "Focus on fat loss with sustainable training and nutrition guidance.",
    ),
    KeepFit(
        id = "keep_fit",
        label = "Keep fit",
        description = "Maintain health, energy, and a balanced routine.",
    ),
    BoostStrength(
        id = "boost_strength",
        label = "Boost strength",
        description = "Improve performance, strength, and capability in the gym.",
    ),
    ManageStress(
        id = "manage_stress",
        label = "Manage stress",
        description = "Support recovery, mood, and consistency with lower-pressure guidance.",
    ),
}
