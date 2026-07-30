package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import java.time.LocalDate
import javax.inject.Inject

class ValidateOnboardingAnswerUseCase @Inject constructor() {
    operator fun invoke(
        stepId: OnboardingStepId,
        answer: OnboardingAnswer?,
    ): Boolean {
        return when (stepId) {
            OnboardingStepIds.IntroWelcome -> true

            OnboardingStepIds.ProfileName -> {
                answer is OnboardingAnswer.Text && answer.value.trim().length in PROFILE_NAME_LENGTH
            }

            OnboardingStepIds.ProfileGender -> {
                answer is OnboardingAnswer.Text && answer.value in PROFILE_GENDER_IDS
            }

            OnboardingStepIds.ProfileDateOfBirth -> {
                answer is OnboardingAnswer.Text && answer.value.isValidDateOfBirth()
            }

            OnboardingStepIds.BodyGoalPrimaryGoal -> {
                answer is OnboardingAnswer.Text && answer.value in PRIMARY_GOAL_IDS
            }

            OnboardingStepIds.BodyGoalHeight -> {
                answer is OnboardingAnswer.Decimal && answer.value in HEIGHT_CM_RANGE
            }

            OnboardingStepIds.BodyGoalCurrentWeight,
            OnboardingStepIds.BodyGoalTargetWeight -> {
                answer is OnboardingAnswer.Decimal && answer.value in WEIGHT_KG_RANGE
            }

            OnboardingStepIds.BodyGoalActivityLevel -> {
                answer is OnboardingAnswer.Text && answer.value in ACTIVITY_LEVEL_IDS
            }

            OnboardingStepIds.BodyGoalHealthCondition -> {
                answer is OnboardingAnswer.Selections &&
                    answer.values.isNotEmpty() &&
                    answer.values.all(HEALTH_CONDITION_IDS::contains) &&
                    (!answer.values.contains("none") || answer.values.size == 1)
            }

            OnboardingStepIds.MobileNumber -> {
                answer is OnboardingAnswer.Text && answer.value.isValidMobileNumber()
            }

            OnboardingStepIds.WorkoutIntroChoice -> {
                answer is OnboardingAnswer.Toggle
            }

            OnboardingStepIds.WorkoutExperience -> {
                answer is OnboardingAnswer.Text && answer.value in WORKOUT_EXPERIENCE_IDS
            }

            OnboardingStepIds.WorkoutGymAccess -> {
                answer is OnboardingAnswer.Text && answer.value in WORKOUT_GYM_ACCESS_IDS
            }

            OnboardingStepIds.WorkoutLocation -> {
                answer is OnboardingAnswer.Text && answer.value in WORKOUT_LOCATION_IDS
            }

            OnboardingStepIds.WorkoutFocusAreas -> {
                answer is OnboardingAnswer.Selections &&
                    answer.values.isNotEmpty() &&
                    answer.values.all(WORKOUT_FOCUS_AREA_IDS::contains) &&
                    answer.values.hasValidWorkoutFocusAreaShape()
            }

            OnboardingStepIds.WorkoutTrainingDays -> {
                answer is OnboardingAnswer.Selections &&
                    answer.values.isNotEmpty() &&
                    answer.values.all(WORKOUT_TRAINING_DAY_IDS::contains)
            }

            OnboardingStepIds.WorkoutDuration -> {
                answer is OnboardingAnswer.Decimal && answer.value in WORKOUT_DURATION_MINUTES
            }

            OnboardingStepIds.WorkoutSplit -> {
                answer is OnboardingAnswer.Text && answer.value in WORKOUT_SPLIT_IDS
            }

            OnboardingStepIds.TargetsStepsTarget -> {
                answer is OnboardingAnswer.Decimal &&
                    answer.value in STEPS_TARGET_RANGE &&
                    answer.value.rem(1.0) == 0.0
            }

            OnboardingStepIds.TargetsSleepTarget -> {
                answer is OnboardingAnswer.Text && answer.value in SLEEP_TARGET_IDS
            }

            OnboardingStepIds.TargetsWaterTarget -> {
                answer is OnboardingAnswer.Decimal &&
                    answer.value in WATER_TARGET_RANGE &&
                    answer.value.rem(1.0) == 0.0
            }

            OnboardingStepIds.TargetsRecommendationSummary -> {
                answer is OnboardingAnswer.Toggle && answer.value
            }

            OnboardingStepIds.TargetsGoalPace -> {
                answer is OnboardingAnswer.Text && answer.value in GOAL_PACE_IDS
            }

            OnboardingStepIds.TargetsNutritionSummary -> {
                answer is OnboardingAnswer.Text && answer.value in NUTRITION_SUMMARY_IDS
            }

            OnboardingStepIds.SourceChannel -> {
                answer is OnboardingAnswer.Text && answer.value in SOURCE_CHANNEL_IDS
            }

            OnboardingStepIds.SourceReason -> {
                answer is OnboardingAnswer.Text && answer.value in SOURCE_REASON_IDS
            }

            OnboardingStepIds.ReviewSummary -> {
                answer is OnboardingAnswer.Toggle && answer.value
            }

            else -> answer.isMeaningful()
        }
    }

    private fun OnboardingAnswer?.isMeaningful(): Boolean {
        return when (this) {
            null -> false
            is OnboardingAnswer.Text -> value.isNotBlank()
            is OnboardingAnswer.Decimal -> true
            is OnboardingAnswer.Selections -> values.isNotEmpty()
            is OnboardingAnswer.Toggle -> true
        }
    }

    private fun String.isValidDateOfBirth(): Boolean {
        return runCatching { LocalDate.parse(this) }
            .getOrNull()
            ?.let { date -> !date.isBefore(EARLIEST_DATE_OF_BIRTH) && date.isBefore(LocalDate.now()) }
            ?: false
    }

    private fun String.isValidMobileNumber(): Boolean {
        val normalizedDigits = filter(Char::isDigit)
        return normalizedDigits.length in MOBILE_NUMBER_LENGTH_RANGE
    }

    private companion object {
        val PROFILE_NAME_LENGTH = 2..30
        val PROFILE_GENDER_IDS = setOf("male", "female", "prefer_not_to_say")
        val PRIMARY_GOAL_IDS = setOf(
            "build_muscle",
            "lose_weight",
            "keep_fit",
            "boost_strength",
            "manage_stress",
        )
        val ACTIVITY_LEVEL_IDS = setOf(
            "sedentary",
            "light",
            "active",
            "very_active",
            "dynamic",
        )
        val WORKOUT_EXPERIENCE_IDS = setOf(
            "fresh",
            "beginner",
            "intermediate",
            "advanced",
        )
        val WORKOUT_GYM_ACCESS_IDS = setOf(
            "gym",
            "home",
            "both",
        )
        val HEALTH_CONDITION_IDS = setOf(
            "none",
            "diabetes",
            "hypertension",
            "low_bp",
            "injury_recovery",
            "other",
        )
        val WORKOUT_LOCATION_IDS = setOf(
            "gym",
            "home",
            "both",
        )
        val WORKOUT_FOCUS_AREA_IDS = setOf(
            "full_body",
            "shoulders",
            "arms",
            "back",
            "chest",
            "abs",
            "glutes",
            "legs",
            "cardio",
        )
        val WORKOUT_TRAINING_DAY_IDS = setOf(
            "monday",
            "tuesday",
            "wednesday",
            "thursday",
            "friday",
            "saturday",
            "sunday",
        )
        val WORKOUT_DURATION_MINUTES = setOf(
            30.0,
            45.0,
            60.0,
            90.0,
            120.0,
        )
        val WORKOUT_SPLIT_IDS = setOf(
            "auto",
            "full_body",
            "upper_lower",
            "ppl",
            "body_part",
        )
        val SOURCE_CHANNEL_IDS = setOf(
            "friend_referral",
            "social_media",
            "search",
            "app_store",
            "coach_or_gym",
            "other",
        )
        val SOURCE_REASON_IDS = setOf(
            "workout_focus",
            "nutrition_focus",
            "complete_reset",
        )
        val GOAL_PACE_IDS = setOf(
            "relaxed",
            "steady",
            "ambitious",
        )
        val SLEEP_TARGET_IDS = setOf(
            "recover_early",
            "balanced_evenings",
            "flexible_late_schedule",
        )
        val NUTRITION_SUMMARY_IDS = setOf(
            "protein_priority",
            "balanced_plate",
            "hydration_consistency",
        )
        val MOBILE_NUMBER_LENGTH_RANGE = 10..15
        val STEPS_TARGET_RANGE = 2000.0..30000.0
        val WATER_TARGET_RANGE = 500.0..6000.0
        val HEIGHT_CM_RANGE = 80.0..260.0
        val WEIGHT_KG_RANGE = 20.0..400.0
        val EARLIEST_DATE_OF_BIRTH: LocalDate = LocalDate.of(1900, 1, 1)
    }
}

private fun List<String>.hasValidWorkoutFocusAreaShape(): Boolean {
    val uniqueValues = toSet()
    val individualAreas = setOf(
        "shoulders",
        "arms",
        "back",
        "chest",
        "abs",
        "glutes",
        "legs",
        "cardio",
    )
    if ("full_body" !in uniqueValues) {
        return true
    }
    return uniqueValues == individualAreas + "full_body"
}
