package com.tnyx.features.nutrition.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class MealItem(
    val id: String,
    val name: String,
    val calories: Int, // Base calories for 1 unit
    val protein: Double, // Base protein for 1 unit
    val quantity: Double,
    val unit: String,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val transFat: Double = 0.0,
    val saturatedFat: Double = 0.0
) {
    val totalCalories: Int get() = (calories * quantity).toInt()
    val totalProtein: Double get() = protein * quantity
}

@Immutable
data class NutritionMeal(
    val id: String,
    val name: String,
    val type: String, // BREAKFAST, LUNCH, etc.
    val imageUrl: String? = null,
    val items: List<MealItem> = emptyList(),
    val servingSize: Double = 1.0,
    val servingsDescription: String = "",
    val description: String = ""
) {
    val totalCalories: Int get() = items.sumOf { it.totalCalories }
    val totalProtein: Double get() = items.sumOf { it.totalProtein }
    val totalFiber: Double get() = items.sumOf { it.fiber * it.quantity }
    val totalCarbs: Double get() = items.sumOf { it.carbs * it.quantity }
    val totalFats: Double get() = items.sumOf { it.fats * it.quantity }
}
