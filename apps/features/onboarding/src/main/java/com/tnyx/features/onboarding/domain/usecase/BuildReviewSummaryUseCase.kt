package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId

data class ReviewSummarySection(
    val title: String,
    val rows: List<ReviewSummaryRow>,
)

data class ReviewSummaryRow(
    val label: String,
    val value: String,
)

class BuildReviewSummaryUseCase {
    operator fun invoke(
        draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    ): List<ReviewSummarySection> {
        val sections = mutableListOf(
            ReviewSummarySection(
                title = "Profile",
                rows = listOf(
                    ReviewSummaryRow(
                        label = "Name",
                        value = draftAnswers.textValue(
                            stepId = OnboardingStepIds.ProfileName,
                            fallback = "Not set",
                        ),
                    ),
                    ReviewSummaryRow(
                        label = "Gender",
                        value = draftAnswers.genderValue(),
                    ),
                    ReviewSummaryRow(
                        label = "Date of birth",
                        value = draftAnswers.textValue(
                            stepId = OnboardingStepIds.ProfileDateOfBirth,
                            fallback = "Not set",
                        ),
                    ),
                ),
            ),
            ReviewSummarySection(
                title = "Body goal",
                rows = listOf(
                    ReviewSummaryRow(
                        label = "Primary goal",
                        value = draftAnswers.primaryGoalValue(),
                    ),
                    ReviewSummaryRow(
                        label = "Height",
                        value = draftAnswers.decimalValue(
                            stepId = OnboardingStepIds.BodyGoalHeight,
                            suffix = " cm",
                        ),
                    ),
                    ReviewSummaryRow(
                        label = "Current weight",
                        value = draftAnswers.decimalValue(
                            stepId = OnboardingStepIds.BodyGoalCurrentWeight,
                            suffix = " kg",
                        ),
                    ),
                    ReviewSummaryRow(
                        label = "Target weight",
                        value = draftAnswers.decimalValue(
                            stepId = OnboardingStepIds.BodyGoalTargetWeight,
                            suffix = " kg",
                        ),
                    ),
                    ReviewSummaryRow(
                        label = "Activity level",
                        value = draftAnswers.activityLevelValue(),
                    ),
                    ReviewSummaryRow(
                        label = "Health context",
                        value = draftAnswers.healthConditionValue(),
                    ),
                ),
            ),
            ReviewSummarySection(
                title = "Mobile",
                rows = listOf(
                    ReviewSummaryRow(
                        label = "Mobile number",
                        value = draftAnswers.textValue(
                            stepId = OnboardingStepIds.MobileNumber,
                            fallback = "Not set",
                        ),
                    ),
                ),
            ),
            ReviewSummarySection(
                title = "Targets",
                rows = listOf(
                    ReviewSummaryRow(
                        label = "Steps target",
                        value = draftAnswers.decimalValue(
                            stepId = OnboardingStepIds.TargetsStepsTarget,
                            suffix = " steps",
                        ),
                    ),
                    ReviewSummaryRow(
                        label = "Sleep target",
                        value = draftAnswers.sleepTargetValue(),
                    ),
                    ReviewSummaryRow(
                        label = "Water target",
                        value = draftAnswers.decimalValue(
                            stepId = OnboardingStepIds.TargetsWaterTarget,
                            suffix = " ml",
                        ),
                    ),
                    ReviewSummaryRow(
                        label = "Goal pace",
                        value = draftAnswers.goalPaceValue(),
                    ),
                    ReviewSummaryRow(
                        label = "Nutrition focus",
                        value = draftAnswers.nutritionSummaryValue(),
                    ),
                ),
            ),
            ReviewSummarySection(
                title = "Source",
                rows = listOf(
                    ReviewSummaryRow(
                        label = "Discovery channel",
                        value = draftAnswers.sourceChannelValue(),
                    ),
                    ReviewSummaryRow(
                        label = "Primary reason",
                        value = draftAnswers.sourceReasonValue(),
                    ),
                ),
            ),
        )

        if (draftAnswers.shouldShowWorkoutSummary()) {
            sections.add(
                3,
                ReviewSummarySection(
                    title = "Workout",
                    rows = listOf(
                        ReviewSummaryRow(
                            label = "Experience",
                            value = draftAnswers.experienceValue(),
                        ),
                        ReviewSummaryRow(
                            label = "Access",
                            value = draftAnswers.gymAccessValue(),
                        ),
                        ReviewSummaryRow(
                            label = "Location",
                            value = draftAnswers.locationValue(),
                        ),
                        ReviewSummaryRow(
                            label = "Focus areas",
                            value = draftAnswers.selectionValue(
                                stepId = OnboardingStepIds.WorkoutFocusAreas,
                                labels = WORKOUT_FOCUS_AREA_LABELS,
                                emptyValue = "Not selected",
                            ),
                        ),
                        *draftAnswers.workoutEquipmentRows().toTypedArray(),
                        ReviewSummaryRow(
                            label = "Training days",
                            value = draftAnswers.selectionValue(
                                stepId = OnboardingStepIds.WorkoutTrainingDays,
                                labels = TRAINING_DAY_LABELS,
                                emptyValue = "Not selected",
                            ),
                        ),
                        ReviewSummaryRow(
                            label = "Duration",
                            value = draftAnswers.durationValue(),
                        ),
                        ReviewSummaryRow(
                            label = "Split",
                            value = draftAnswers.workoutSplitValue(),
                        ),
                        ReviewSummaryRow(
                            label = "Workout concerns",
                            value = draftAnswers.textValue(
                                stepId = OnboardingStepIds.WorkoutHealthConcerns,
                                fallback = "Not shared",
                            ),
                        ),
                        ReviewSummaryRow(
                            label = "Special event",
                            value = draftAnswers.textValue(
                                stepId = OnboardingStepIds.WorkoutSpecialEventGoal,
                                fallback = "Not shared",
                            ),
                        ),
                    ),
                ),
            )
        }

        return sections
    }
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

private fun Map<OnboardingStepId, OnboardingAnswer>.healthConditionValue(): String {
    val values = (this[OnboardingStepIds.BodyGoalHealthCondition] as? OnboardingAnswer.Selections)
        ?.values
        .orEmpty()
    if (values.isEmpty()) return "Not set"

    return values.joinToString(separator = ", ") { value ->
        when (value) {
            "none" -> "No current condition"
            "diabetes" -> "Diabetes"
            "hypertension" -> "Hypertension"
            "low_bp" -> "Low blood pressure"
            "injury_recovery" -> "Injury recovery"
            "other" -> "Other health context"
            else -> value
        }
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

private fun Map<OnboardingStepId, OnboardingAnswer>.gymAccessValue(): String {
    return when ((this[OnboardingStepIds.WorkoutGymAccess] as? OnboardingAnswer.Text)?.value) {
        "gym" -> "Gym only"
        "home" -> "Home only"
        "both" -> "Both"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.locationValue(): String {
    return when ((this[OnboardingStepIds.WorkoutLocation] as? OnboardingAnswer.Text)?.value) {
        "gym" -> "Gym focused"
        "home" -> "Home focused"
        "both" -> "Flexible mix"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.workoutEquipmentRows(): List<ReviewSummaryRow> {
    if ((this[OnboardingStepIds.WorkoutGymAccess] as? OnboardingAnswer.Text)?.value == "gym") {
        return emptyList()
    }
    return listOf(
        ReviewSummaryRow(
            label = "Equipment",
            value = selectionValue(
                stepId = OnboardingStepIds.WorkoutEquipment,
                labels = WORKOUT_EQUIPMENT_LABELS,
                emptyValue = "Not selected",
            ),
        ),
    )
}

private fun Map<OnboardingStepId, OnboardingAnswer>.durationValue(): String {
    val value = (this[OnboardingStepIds.WorkoutDuration] as? OnboardingAnswer.Decimal)?.value
        ?: return "Not set"
    return value.toDisplayValue() + " minutes"
}

private fun Map<OnboardingStepId, OnboardingAnswer>.workoutSplitValue(): String {
    return when ((this[OnboardingStepIds.WorkoutSplit] as? OnboardingAnswer.Text)?.value) {
        "auto" -> "Recommend for me"
        "full_body" -> "Full body"
        "upper_lower" -> "Upper / lower"
        "ppl" -> "Push / pull / legs"
        "body_part" -> "Body part split"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.goalPaceValue(): String {
    return when ((this[OnboardingStepIds.TargetsGoalPace] as? OnboardingAnswer.Text)?.value) {
        "relaxed" -> "Relaxed"
        "steady" -> "Steady"
        "ambitious" -> "Ambitious"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.sleepTargetValue(): String {
    return when ((this[OnboardingStepIds.TargetsSleepTarget] as? OnboardingAnswer.Text)?.value) {
        "recover_early" -> "Early recovery"
        "balanced_evenings" -> "Balanced evenings"
        "flexible_late_schedule" -> "Late schedule"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.nutritionSummaryValue(): String {
    return when ((this[OnboardingStepIds.TargetsNutritionSummary] as? OnboardingAnswer.Text)?.value) {
        "protein_priority" -> "Protein priority"
        "balanced_plate" -> "Balanced plate"
        "hydration_consistency" -> "Hydration first"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.sourceChannelValue(): String {
    return when ((this[OnboardingStepIds.SourceChannel] as? OnboardingAnswer.Text)?.value) {
        "friend_referral" -> "Friend referral"
        "social_media" -> "Social media"
        "search" -> "Search"
        "app_store" -> "App store"
        "coach_or_gym" -> "Coach or gym"
        "other" -> "Other"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.sourceReasonValue(): String {
    return when ((this[OnboardingStepIds.SourceReason] as? OnboardingAnswer.Text)?.value) {
        "workout_focus" -> "Workout focus"
        "nutrition_focus" -> "Nutrition focus"
        "complete_reset" -> "Complete reset"
        else -> "Not set"
    }
}

private fun Map<OnboardingStepId, OnboardingAnswer>.shouldShowWorkoutSummary(): Boolean {
    return (this[OnboardingStepIds.WorkoutIntroChoice] as? OnboardingAnswer.Toggle)?.value != false
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

private val WORKOUT_FOCUS_AREA_LABELS = mapOf(
    "full_body" to "Full body",
    "shoulders" to "Shoulders",
    "arms" to "Arms",
    "back" to "Back",
    "chest" to "Chest",
    "abs" to "Abs",
    "glutes" to "Glutes",
    "legs" to "Legs",
    "cardio" to "Cardio",
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
