package com.tnyx.features.nutrition.presentation.nutrition_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NutritionOverviewViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionOverviewUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<NutritionOverviewEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: NutritionOverviewAction) {
        when (action) {
            NutritionOverviewAction.BackClicked -> {
                viewModelScope.launch {
                    _effect.emit(NutritionOverviewEffect.NavigateBack)
                }
            }
            is NutritionOverviewAction.DateSelected -> {
                _uiState.update { it.copy(selectedDate = action.date) }
                loadOverview(action.date)
            }
            is NutritionOverviewAction.TabSelected -> {
                _uiState.update { it.copy(targetNutrient = action.nutrient) }
            }
        }
    }

    private fun loadOverview(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val snapshot = nutritionRepository.getMealDiary(date)
            _uiState.update {
                it.copy(
                    selectedDate = date,
                    caloriesConsumed = snapshot.meals.sumOf { meal -> meal.totalCalories },
                    caloriesGoal = snapshot.caloriesGoal,
                    proteinConsumed = snapshot.meals.sumOf { meal -> meal.totalProtein },
                    proteinGoal = snapshot.proteinGoal,
                    carbsConsumed = snapshot.meals.sumOf { meal -> meal.totalCarbs },
                    carbsGoal = snapshot.carbsGoal,
                    fatsConsumed = snapshot.meals.sumOf { meal -> meal.totalFats },
                    fatsGoal = snapshot.fatsGoal,
                    fiberConsumed = snapshot.meals.sumOf { meal -> meal.totalFiber },
                    fiberGoal = snapshot.fiberGoal,
                    waterConsumed = snapshot.waterConsumedLiters,
                    waterGoal = snapshot.waterGoalLiters,
                    isLoading = false
                )
            }
        }
    }
}
