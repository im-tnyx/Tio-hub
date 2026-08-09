package com.tnyx.features.nutrition.presentation.nutrition_overview

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class NutritionOverviewUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val targetNutrient: String = "all", // "all", "calories", "protein", "carbs", "fats", "fiber", "water"
    val caloriesConsumed: Int = 0,
    val caloriesGoal: Int = 1566,
    val proteinConsumed: Double = 0.0,
    val proteinGoal: Double = 140.0,
    val carbsConsumed: Double = 0.0,
    val carbsGoal: Double = 196.0,
    val fatsConsumed: Double = 0.0,
    val fatsGoal: Double = 38.0,
    val fiberConsumed: Double = 0.0,
    val fiberGoal: Double = 30.0,
    val waterConsumed: Double = 0.0,
    val waterGoal: Double = 3.0,
    val isLoading: Boolean = false,
)

sealed interface NutritionOverviewAction {
    data object BackClicked : NutritionOverviewAction
    data class DateSelected(val date: LocalDate) : NutritionOverviewAction
    data class TabSelected(val nutrient: String) : NutritionOverviewAction
}

sealed interface NutritionOverviewEffect {
    data object NavigateBack : NutritionOverviewEffect
}
