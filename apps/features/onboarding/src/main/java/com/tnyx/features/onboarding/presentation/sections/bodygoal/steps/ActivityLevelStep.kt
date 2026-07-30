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
internal fun ActivityLevelStep(
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
            title = "How active are you most days?",
            description = "Your day-to-day movement helps Tio estimate calories, recovery, and effort.",
        )
        ActivityLevelOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                badge = option.badge,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one activity level to continue")
        }
    }
}

private enum class ActivityLevelOption(
    val id: String,
    val label: String,
    val badge: String,
    val description: String,
) {
    Sedentary(
        id = "sedentary",
        label = "Sedentary",
        badge = "Mostly seated",
        description = "Desk-heavy routine with little walking or planned movement.",
    ),
    Light(
        id = "light",
        label = "Light",
        badge = "Light movement",
        description = "Some walking or chores, but not much structured activity yet.",
    ),
    Active(
        id = "active",
        label = "Active",
        badge = "Regular movement",
        description = "Frequent walking or workouts a few times each week.",
    ),
    VeryActive(
        id = "very_active",
        label = "Very active",
        badge = "High routine",
        description = "Hard training, physical work, or consistently high daily movement.",
    ),
    Dynamic(
        id = "dynamic",
        label = "Dynamic",
        badge = "Athletic load",
        description = "Very high output days with intense training or demanding activity.",
    ),
}
