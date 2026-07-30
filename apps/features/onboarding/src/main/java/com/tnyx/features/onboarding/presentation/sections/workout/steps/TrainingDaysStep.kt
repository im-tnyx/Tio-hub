package com.tnyx.features.onboarding.presentation.sections.workout.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingSelectionMode
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun TrainingDaysStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = (answer as? OnboardingAnswer.Selections)?.values.orEmpty().toSet()
    val orderedIds = TrainingDayOption.entries.map { it.id }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Which days can you usually train?",
            description = "Choose every day that realistically works for your weekly routine.",
        )
        TrainingDayOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedIds.contains(option.id),
                selectionMode = OnboardingSelectionMode.Multiple,
                onClick = {
                    onAnswerChanged(
                        selectedIds.toggleWorkoutSelection(
                            optionId = option.id,
                            orderedIds = orderedIds,
                        ),
                    )
                },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select at least one training day to continue")
        }
    }
}

private enum class TrainingDayOption(
    val id: String,
    val label: String,
    val description: String,
) {
    Monday(
        id = "monday",
        label = "Monday",
        description = "Strong start to the week.",
    ),
    Tuesday(
        id = "tuesday",
        label = "Tuesday",
        description = "Useful for building early-week consistency.",
    ),
    Wednesday(
        id = "wednesday",
        label = "Wednesday",
        description = "A balanced mid-week option.",
    ),
    Thursday(
        id = "thursday",
        label = "Thursday",
        description = "Great for spacing sessions across the week.",
    ),
    Friday(
        id = "friday",
        label = "Friday",
        description = "Helpful if weekends need to stay lighter.",
    ),
    Saturday(
        id = "saturday",
        label = "Saturday",
        description = "Good for longer or more relaxed sessions.",
    ),
    Sunday(
        id = "sunday",
        label = "Sunday",
        description = "Useful for weekend planning and prep.",
    ),
}
