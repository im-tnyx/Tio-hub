package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealDiaryViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val initialDate: LocalDate = LocalDate.now(),
) : ViewModel() {

    private var loadJob: Job? = null
    private var refreshJob: Job? = null

    private val _uiState = MutableStateFlow(
        MealDiaryUiState(
            selectedDate = initialDate,
            weekDays = weekDaysAround(initialDate),
            isLoading = true,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealDiaryEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadDiary(initialDate)
        startAutoRefresh()
    }

    fun handleAction(action: MealDiaryAction) {
        when (action) {
            is MealDiaryAction.DateSelected -> {
                loadDiary(action.date)
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
            MealDiaryAction.FabToggled -> {
                _uiState.update { it.copy(isFabExpanded = !it.isFabExpanded) }
            }
            MealDiaryAction.FabCollapsed -> {
                _uiState.update { it.copy(isFabExpanded = false) }
            }
            MealDiaryAction.AddMealClicked,
            MealDiaryAction.AddMealVoiceClicked,
            MealDiaryAction.AddMealCameraClicked -> {
                _uiState.update { it.copy(isFabExpanded = false) }
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToSearch(_uiState.value.selectedDate))
                }
            }

        }
    }

    private fun loadDiary(date: LocalDate) {
        loadDiary(date = date, showLoading = true)
    }

    private fun loadDiary(
        date: LocalDate,
        showLoading: Boolean,
    ) {
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                selectedDate = date,
                weekDays = weekDaysAround(date),
                isLoading = showLoading,
            )
        }
        loadJob = viewModelScope.launch {
            val snapshot = nutritionRepository.getMealDiary(date)
            _uiState.value = snapshot.toUiState()
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(10_000L)
                loadDiary(
                    date = _uiState.value.selectedDate,
                    showLoading = false,
                )
            }
        }
    }

    private fun MealDiarySnapshot.toUiState(): MealDiaryUiState {
        return MealDiaryUiState(
            selectedDate = selectedDate,
            weekDays = weekDaysAround(selectedDate),
            hasDietPlan = hasDietPlan,
            caloriesConsumed = meals.sumOf(NutritionMeal::totalCalories),
            caloriesGoal = caloriesGoal,
            proteinConsumed = meals.sumOf(NutritionMeal::totalProtein),
            proteinGoal = proteinGoal,
            fiberConsumed = meals.sumOf(NutritionMeal::totalFiber),
            fiberGoal = fiberGoal,
            carbsConsumed = meals.sumOf(NutritionMeal::totalCarbs),
            carbsGoal = carbsGoal,
            sugarConsumed = meals.sumOf { meal ->
                meal.items.sumOf { item -> item.sugar * item.quantity }
            },
            sugarGoal = sugarGoal,
            fatsConsumed = meals.sumOf(NutritionMeal::totalFats),
            fatsGoal = fatsGoal,
            waterConsumed = waterConsumedLiters,
            waterGoal = waterGoalLiters,
            vitaminsProgress = vitaminsProgress,
            mineralsProgress = mineralsProgress,
            meals = meals,
            isLoading = false,
            isFabExpanded = false, // Always collapse FAB after a diary reload
        )
    }

    private fun weekDaysAround(date: LocalDate): List<LocalDate> {
        return (0..6).map { offset ->
            date.minusDays(6L - offset.toLong())
        }
    }
}
