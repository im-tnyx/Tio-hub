package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingTargetSnapshot
import javax.inject.Inject
import kotlin.math.roundToInt

class AutoCalculateOnboardingTargetsUseCase @Inject constructor() {
    operator fun invoke(
        draft: OnboardingDraft,
    ): OnboardingTargetSnapshot {
        val activityLevel = (draft.answerFor(OnboardingStepIds.BodyGoalActivityLevel) as? OnboardingAnswer.Text)
            ?.value
        val primaryGoal = (draft.answerFor(OnboardingStepIds.BodyGoalPrimaryGoal) as? OnboardingAnswer.Text)
            ?.value
        val currentWeightKg = (draft.answerFor(OnboardingStepIds.BodyGoalCurrentWeight) as? OnboardingAnswer.Decimal)
            ?.value
            ?: 70.0

        val stepsTarget = baseStepsTarget(activityLevel, primaryGoal)
        val sleepTargetId = recommendedSleepTarget(activityLevel, primaryGoal)
        val waterTargetMl = recommendedWaterTarget(currentWeightKg, activityLevel)
        val goalPaceKgPerWeek = recommendedGoalPace(primaryGoal)
        val caloriesTarget = recommendedCalories(currentWeightKg, activityLevel, primaryGoal)
        val proteinTargetGrams = recommendedProtein(currentWeightKg, primaryGoal)
        val fatTargetGrams = (caloriesTarget * 0.25 / 9.0).roundToInt().coerceAtLeast(35)
        val carbsTargetGrams = ((caloriesTarget - (proteinTargetGrams * 4) - (fatTargetGrams * 9)) / 4.0)
            .roundToInt()
            .coerceAtLeast(80)
        val fiberTargetGrams = when (primaryGoal) {
            "lose_weight" -> 32
            "manage_stress" -> 30
            else -> 28
        }

        return OnboardingTargetSnapshot(
            stepsTarget = stepsTarget,
            sleepTargetId = sleepTargetId,
            waterTargetMl = waterTargetMl,
            caloriesTarget = caloriesTarget,
            proteinTargetGrams = proteinTargetGrams,
            carbsTargetGrams = carbsTargetGrams,
            fatTargetGrams = fatTargetGrams,
            fiberTargetGrams = fiberTargetGrams,
            goalPaceKgPerWeek = goalPaceKgPerWeek,
        )
    }

    private fun baseStepsTarget(
        activityLevel: String?,
        primaryGoal: String?,
    ): Int {
        val baseTarget = when (activityLevel) {
            "sedentary" -> 6000
            "light" -> 8000
            "active" -> 10000
            "very_active" -> 12000
            "dynamic" -> 13000
            else -> 8000
        }

        return when (primaryGoal) {
            "lose_weight" -> baseTarget + 1000
            "manage_stress" -> baseTarget - 1000
            else -> baseTarget
        }.coerceIn(4000, 16000)
    }

    private fun recommendedSleepTarget(
        activityLevel: String?,
        primaryGoal: String?,
    ): String {
        return when {
            primaryGoal == "manage_stress" -> "recover_early"
            activityLevel == "very_active" -> "recover_early"
            activityLevel == "dynamic" -> "flexible_late_schedule"
            else -> "balanced_evenings"
        }
    }

    private fun recommendedWaterTarget(
        currentWeightKg: Double,
        activityLevel: String?,
    ): Int {
        val baseMl = if (currentWeightKg > 0.0) {
            currentWeightKg * 35.0
        } else {
            when (activityLevel) {
                "sedentary" -> 2200.0
                "light" -> 2500.0
                "active" -> 3000.0
                "very_active" -> 3500.0
                "dynamic" -> 3750.0
                else -> 2500.0
            }
        }

        return ((baseMl / 250.0).roundToInt() * 250).coerceIn(1500, 5000)
    }

    private fun recommendedGoalPace(primaryGoal: String?): Double {
        return when (primaryGoal) {
            "lose_weight" -> 0.5
            "build_muscle" -> 0.25
            "boost_strength" -> 0.25
            else -> 0.25
        }
    }

    private fun recommendedCalories(
        currentWeightKg: Double,
        activityLevel: String?,
        primaryGoal: String?,
    ): Int {
        val multiplier = when (activityLevel) {
            "sedentary" -> 28.0
            "light" -> 31.0
            "active" -> 34.0
            "very_active" -> 36.0
            "dynamic" -> 38.0
            else -> 31.0
        }

        val maintenance = (currentWeightKg * multiplier).roundToInt()
        val adjusted = when (primaryGoal) {
            "lose_weight" -> maintenance - 300
            "build_muscle" -> maintenance + 200
            "boost_strength" -> maintenance + 150
            else -> maintenance
        }
        return adjusted.coerceIn(1400, 4200)
    }

    private fun recommendedProtein(
        currentWeightKg: Double,
        primaryGoal: String?,
    ): Int {
        val multiplier = when (primaryGoal) {
            "build_muscle" -> 2.0
            "lose_weight" -> 1.8
            "boost_strength" -> 1.9
            else -> 1.6
        }
        return (currentWeightKg * multiplier).roundToInt().coerceIn(75, 240)
    }
}
