package com.tnyx.features.onboarding.presentation.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingSelectionMode
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun WorkoutStepContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.WorkoutExperience -> WorkoutExperienceStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutLocation -> WorkoutLocationStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutEquipment -> WorkoutEquipmentStep(
            answer = answer,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutTrainingDays -> WorkoutTrainingDaysStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutDuration -> WorkoutDurationStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}

@Composable
private fun WorkoutExperienceStep(
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

@Composable
private fun WorkoutLocationStep(
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
            title = "Where will you usually train?",
            description = "Your training location shapes equipment expectations and plan flexibility.",
        )
        WorkoutLocationOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one workout location to continue")
        }
    }
}

@Composable
private fun WorkoutEquipmentStep(
    answer: OnboardingAnswer?,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = (answer as? OnboardingAnswer.Selections)?.values.orEmpty().toSet()

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
                        selectedIds.toggleSelection(
                            optionId = option.id,
                            orderedIds = WorkoutEquipmentOption.entries.map { it.id },
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

@Composable
private fun WorkoutTrainingDaysStep(
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
                        selectedIds.toggleSelection(
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

@Composable
private fun WorkoutDurationStep(
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

private fun Set<String>.toggleSelection(
    optionId: String,
    orderedIds: List<String>,
): OnboardingAnswer.Selections? {
    val updated = toMutableSet().apply {
        if (!add(optionId)) {
            remove(optionId)
        }
    }
    if (updated.isEmpty()) return null
    val ordered = orderedIds.filter(updated::contains)
    return OnboardingAnswer.Selections(ordered)
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

private enum class WorkoutLocationOption(
    val id: String,
    val label: String,
    val description: String,
) {
    Gym(
        id = "gym",
        label = "Gym",
        description = "You mostly train in a gym with broad equipment access.",
    ),
    Home(
        id = "home",
        label = "Home",
        description = "You prefer home workouts and need plans that fit your setup.",
    ),
    Both(
        id = "both",
        label = "Both",
        description = "You switch between home and gym and need flexible programming.",
    ),
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
