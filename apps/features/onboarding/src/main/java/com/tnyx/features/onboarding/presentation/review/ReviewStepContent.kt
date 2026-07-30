package com.tnyx.features.onboarding.presentation.review

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
internal fun ReviewStepContent(
    answer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isConfirmed = (answer as? OnboardingAnswer.Toggle)?.value == true

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "Review your setup",
            description = "Check your details once, then finish onboarding and let Tio use these as your starting preferences.",
        )
        SummarySection(
            title = "Profile",
            rows = listOf(
                "Name" to draftAnswers.textValue(OnboardingStepIds.ProfileName, fallback = "Not set"),
                "Gender" to draftAnswers.genderValue(),
                "Date of birth" to draftAnswers.textValue(
                    OnboardingStepIds.ProfileDateOfBirth,
                    fallback = "Not set",
                ),
            ),
        )
        SummarySection(
            title = "Body goal",
            rows = listOf(
                "Primary goal" to draftAnswers.primaryGoalValue(),
                "Height" to draftAnswers.decimalValue(
                    OnboardingStepIds.BodyGoalHeight,
                    suffix = " cm",
                ),
                "Current weight" to draftAnswers.decimalValue(
                    OnboardingStepIds.BodyGoalCurrentWeight,
                    suffix = " kg",
                ),
                "Target weight" to draftAnswers.decimalValue(
                    OnboardingStepIds.BodyGoalTargetWeight,
                    suffix = " kg",
                ),
                "Activity level" to draftAnswers.activityLevelValue(),
            ),
        )
        SummarySection(
            title = "Workout",
            rows = listOf(
                "Experience" to draftAnswers.experienceValue(),
                "Location" to draftAnswers.locationValue(),
                "Equipment" to draftAnswers.selectionValue(
                    stepId = OnboardingStepIds.WorkoutEquipment,
                    labels = WORKOUT_EQUIPMENT_LABELS,
                    emptyValue = "Not selected",
                ),
                "Training days" to draftAnswers.selectionValue(
                    stepId = OnboardingStepIds.WorkoutTrainingDays,
                    labels = TRAINING_DAY_LABELS,
                    emptyValue = "Not selected",
                ),
                "Duration" to draftAnswers.durationValue(),
            ),
        )
        OnboardingChoiceCard(
            title = "Everything looks right",
            description = "I understand these details can be refined later in the app.",
            selected = isConfirmed,
            selectionMode = OnboardingSelectionMode.Multiple,
            onClick = {
                onAnswerChanged(
                    if (isConfirmed) {
                        null
                    } else {
                        OnboardingAnswer.Toggle(true)
                    },
                )
            },
        )
        if (showValidationError) {
            OnboardingValidationMessage("Confirm the review to finish onboarding")
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    rows: List<Pair<String, String>>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        Text(
            text = title,
            style = TnyxTheme.typography.titleLarge,
            color = TnyxTheme.colors.textPrimary,
        )
        rows.forEach { (label, value) ->
            SummaryRow(
                label = label,
                value = value,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    OnboardingChoiceCard(
        title = label,
        description = value,
        selected = false,
        onClick = null,
        showSelectionControl = false,
    )
}

private fun Map<OnboardingStepId, OnboardingAnswer>.textValue(
    stepId: OnboardingStepId,
    fallback: String,
): String {
    return (this[stepId] as? OnboardingAnswer.Text)?.value?.ifBlank { fallback } ?: fallback
}

private fun Map<OnboardingStepId, OnboardingAnswer>.decimalValue(
    stepId: OnboardingStepId,
    suffix: String,
): String {
    val value = (this[stepId] as? OnboardingAnswer.Decimal)?.value ?: return "Not set"
    return value.toDisplayValue() + suffix
}

private fun Map<OnboardingStepId, OnboardingAnswer>.selectionValue(
    stepId: OnboardingStepId,
    labels: Map<String, String>,
    emptyValue: String,
): String {
    val values = (this[stepId] as? OnboardingAnswer.Selections)?.values.orEmpty()
    if (values.isEmpty()) return emptyValue
    return values.joinToString(separator = ", ") { value ->
        labels[value] ?: value
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.genderValue(): String {
    return when ((this[OnboardingStepIds.ProfileGender] as? OnboardingAnswer.Text)?.value) {
        "male" -> "Male"
        "female" -> "Female"
        "prefer_not_to_say" -> "Prefer not to say"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.primaryGoalValue(): String {
    return when ((this[OnboardingStepIds.BodyGoalPrimaryGoal] as? OnboardingAnswer.Text)?.value) {
        "build_muscle" -> "Build muscle"
        "lose_weight" -> "Lose weight"
        "keep_fit" -> "Keep fit"
        "boost_strength" -> "Boost strength"
        "manage_stress" -> "Manage stress"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.activityLevelValue(): String {
    return when ((this[OnboardingStepIds.BodyGoalActivityLevel] as? OnboardingAnswer.Text)?.value) {
        "sedentary" -> "Sedentary"
        "light" -> "Light"
        "active" -> "Active"
        "very_active" -> "Very active"
        "dynamic" -> "Dynamic"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.experienceValue(): String {
    return when ((this[OnboardingStepIds.WorkoutExperience] as? OnboardingAnswer.Text)?.value) {
        "fresh" -> "Fresh"
        "beginner" -> "Beginner"
        "intermediate" -> "Intermediate"
        "advanced" -> "Advanced"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.locationValue(): String {
    return when ((this[OnboardingStepIds.WorkoutLocation] as? OnboardingAnswer.Text)?.value) {
        "gym" -> "Gym"
        "home" -> "Home"
        "both" -> "Both"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.durationValue(): String {
    val value = (this[OnboardingStepIds.WorkoutDuration] as? OnboardingAnswer.Decimal)?.value
        ?: return "Not set"
    return value.toDisplayValue() + " minutes"
}

private fun Double.toDisplayValue(): String {
    return if (rem(1.0) == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

private val WORKOUT_EQUIPMENT_LABELS = mapOf(
    "dumbbells" to "Dumbbells",
    "bench" to "Bench",
    "mat" to "Mat",
    "barbell" to "Barbell",
    "bands" to "Bands",
    "kettlebell" to "Kettlebell",
)

private val TRAINING_DAY_LABELS = mapOf(
    "monday" to "Monday",
    "tuesday" to "Tuesday",
    "wednesday" to "Wednesday",
    "thursday" to "Thursday",
    "friday" to "Friday",
    "saturday" to "Saturday",
    "sunday" to "Sunday",
)
