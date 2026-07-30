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
internal fun FocusAreasStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = (answer as? OnboardingAnswer.Selections)?.values.orEmpty().toSet()
    val orderedIds = WorkoutFocusAreaOption.entries.map { it.id }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Which areas do you want to focus on most?",
            description = "Choose the body areas or training focus that matter most right now.",
        )
        WorkoutFocusAreaOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedIds.contains(option.id),
                selectionMode = OnboardingSelectionMode.Multiple,
                onClick = {
                    onAnswerChanged(
                        selectedIds.toggleFocusAreaSelection(
                            optionId = option.id,
                            orderedIds = orderedIds,
                        ),
                    )
                },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose at least one focus area to continue")
        }
    }
}

private enum class WorkoutFocusAreaOption(
    val id: String,
    val label: String,
    val description: String,
) {
    FullBody(
        id = "full_body",
        label = "Full body",
        description = "Useful when you want balanced coverage instead of isolating one area.",
    ),
    Shoulders(
        id = "shoulders",
        label = "Shoulders",
        description = "Helpful if upper-body shape and pressing strength matter more.",
    ),
    Arms(
        id = "arms",
        label = "Arms",
        description = "Useful when arm size, definition, or direct accessory work matters.",
    ),
    Back(
        id = "back",
        label = "Back",
        description = "Great for posture, pulling strength, and upper-body structure.",
    ),
    Chest(
        id = "chest",
        label = "Chest",
        description = "Useful when pressing strength and upper-body aesthetics are a priority.",
    ),
    Abs(
        id = "abs",
        label = "Abs",
        description = "Helpful when core control, trunk strength, or visible definition matters.",
    ),
    Glutes(
        id = "glutes",
        label = "Glutes",
        description = "Useful for lower-body strength, shape, and hip-driven power work.",
    ),
    Legs(
        id = "legs",
        label = "Legs",
        description = "Choose this when lower-body strength or muscle gain is a priority.",
    ),
    Cardio(
        id = "cardio",
        label = "Cardio",
        description = "Useful when endurance, conditioning, or calorie burn matters most.",
    ),
}

private fun Set<String>.toggleFocusAreaSelection(
    optionId: String,
    orderedIds: List<String>,
): OnboardingAnswer.Selections? {
    val updated = toMutableSet()
    val allIndividualIds = orderedIds.filterNot { it == "full_body" }

    if (optionId == "full_body") {
        return if (contains("full_body")) {
            null
        } else {
            OnboardingAnswer.Selections(listOf("full_body") + allIndividualIds)
        }
    }

    updated.remove("full_body")
    if (!updated.add(optionId)) {
        updated.remove(optionId)
    }
    if (updated.isEmpty()) return null

    if (updated.containsAll(allIndividualIds)) {
        updated.add("full_body")
    }

    val ordered = orderedIds.filter(updated::contains)
    return OnboardingAnswer.Selections(ordered)
}
