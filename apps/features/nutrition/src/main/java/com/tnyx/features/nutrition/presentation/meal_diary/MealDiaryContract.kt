package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.compose.runtime.Immutable
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import java.time.LocalDate

@Immutable
data class MealDiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<LocalDate> = emptyList(),
    val hasDietPlan: Boolean = false,

    // Macros & Calories
    val caloriesConsumed: Int = 0,
    val caloriesGoal: Int = 1566,
    val proteinConsumed: Double = 0.0,
    val proteinGoal: Double = 140.0,
    val fiberConsumed: Double = 0.0,
    val fiberGoal: Double = 30.0,
    val carbsConsumed: Double = 0.0,
    val carbsGoal: Double = 196.0,
    val sugarConsumed: Double = 0.0,
    val sugarGoal: Double = 38.0,
    val fatsConsumed: Double = 0.0,
    val fatsGoal: Double = 38.0,
    val waterConsumed: Double = 0.0,
    val waterGoal: Double = 3.0,

    // Micros (Vitamins & Minerals)
    val vitaminsProgress: Double = 0.0,
    val mineralsProgress: Double = 0.0,

    // Logged Meals
    val meals: List<NutritionMeal> = emptyList(),
    val isLoading: Boolean = false
) {
    val isHistoryEmpty: Boolean get() = meals.isEmpty()
}

sealed class MealDiaryAction {
    data class DateSelected(val date: LocalDate) : MealDiaryAction()
    data class MealClicked(val meal: NutritionMeal) : MealDiaryAction()
    data class OverviewRequested(val target: String) : MealDiaryAction()
    data object AddMealClicked : MealDiaryAction()
}

sealed class MealDiaryEffect {
    data class NavigateToMealDetail(val mealId: String) : MealDiaryEffect()
    data object NavigateToAddMeal : MealDiaryEffect()
    data class ShowOverview(val target: String) : MealDiaryEffect()
}
