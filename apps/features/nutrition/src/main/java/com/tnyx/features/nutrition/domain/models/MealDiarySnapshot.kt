package com.tnyx.features.nutrition.domain.models

import java.time.LocalDate

data class MealDiarySnapshot(
    val selectedDate: LocalDate,
    val hasDietPlan: Boolean,
    val caloriesGoal: Int,
    val proteinGoal: Double,
    val fiberGoal: Double,
    val carbsGoal: Double,
    val sugarGoal: Double,
    val fatsGoal: Double,
    val waterConsumedLiters: Double,
    val waterGoalLiters: Double,
    val vitaminsProgress: Double,
    val mineralsProgress: Double,
    val meals: List<NutritionMeal>,
    val micronutrientsConsumed: MicronutrientSnapshot = MicronutrientSnapshot(),
    val micronutrientTargets: MicronutrientSnapshot = MicronutrientSnapshot(),
    val sodiumConsumedMg: Double? = null,
    val sodiumLimitMg: Double? = null,
    val nutritionReferenceStatus: String = "unavailable",
)
