package com.tnyx.features.onboarding.presentation.sections.workout.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingSelectionMode
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun EquipmentStep(
    answer: OnboardingAnswer?,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = (answer as? OnboardingAnswer.Selections)?.values.orEmpty().toSet()
    val orderedIds = WorkoutEquipmentOption.entries.map { it.id }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Which equipment do you already have?",
            description = "This step is optional. Pick anything you can use regularly so Tio can adapt better.",
        )
        WorkoutEquipmentOption.entries.forEach { option ->
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
        Text(
            text = "Optional for now. You can skip this section or leave this step empty.",
            style = TnyxTheme.typography.bodySmall,
            color = TnyxTheme.colors.textMuted,
        )
    }
}

private enum class WorkoutEquipmentOption(
    val id: String,
    val label: String,
    val description: String,
) {
    Dumbbells(
        id = "dumbbells",
        label = "Dumbbells",
        description = "Adjustable or fixed dumbbells you can use regularly.",
    ),
    Bench(
        id = "bench",
        label = "Bench",
        description = "A flat or adjustable bench for presses, rows, and support.",
    ),
    Mat(
        id = "mat",
        label = "Mat",
        description = "Useful for mobility, floor work, and core sessions.",
    ),
    Barbell(
        id = "barbell",
        label = "Barbell",
        description = "A barbell setup for compound lifts and strength work.",
    ),
    Bands(
        id = "bands",
        label = "Bands",
        description = "Resistance bands for warmups, accessories, and travel workouts.",
    ),
    Kettlebell(
        id = "kettlebell",
        label = "Kettlebell",
        description = "Good for conditioning, carries, and full-body movement.",
    ),
}
