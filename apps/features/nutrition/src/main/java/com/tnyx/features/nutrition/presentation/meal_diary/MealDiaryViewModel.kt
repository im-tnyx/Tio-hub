package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealDiaryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MealDiaryUiState(
        weekDays = (0..6).map { LocalDate.now().minusDays(it.toLong()) }.reversed(),
        meals = listOf(
            NutritionMeal(
                id = "1",
                name = "Avocado Toast",
                type = "BREAKFAST",
                items = listOf(
                    MealItem(id = "i1", name = "Whole Grain Bread", calories = 120, protein = 4.0, quantity = 2.0, unit = "slice"),
                    MealItem(id = "i2", name = "Avocado", calories = 160, protein = 2.0, quantity = 0.5, unit = "fruit")
                )
            ),
            NutritionMeal(
                id = "2",
                name = "Grilled Chicken Salad",
                type = "LUNCH",
                items = listOf(
                    MealItem(id = "i3", name = "Chicken Breast", calories = 165, protein = 31.0, quantity = 1.0, unit = "100g"),
                    MealItem(id = "i4", name = "Mixed Greens", calories = 15, protein = 1.0, quantity = 2.0, unit = "cup")
                )
            )
        ),
        caloriesConsumed = 460,
        proteinConsumed = 42.0,
        carbsConsumed = 35.0,
        fatsConsumed = 18.0
    ))
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealDiaryEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: MealDiaryAction) {
        when (action) {
            is MealDiaryAction.DateSelected -> {
                _uiState.update { it.copy(selectedDate = action.date) }
                // Fetch meals for the selected date
            }
            is MealDiaryAction.MealClicked -> {
                viewModelScope.launch { 
                    _effect.emit(MealDiaryEffect.NavigateToMealDetail(action.meal.id))
                }
            }
            is MealDiaryAction.OverviewRequested -> {
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.ShowOverview(action.target))
                }
            }
            MealDiaryAction.AddMealClicked -> {
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToAddMeal)
                }
            }
        }
    }
}
