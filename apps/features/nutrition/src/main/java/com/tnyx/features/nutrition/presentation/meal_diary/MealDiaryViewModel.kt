package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MicronutrientSnapshot
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MealDiaryViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val initialDate: LocalDate = LocalDate.now(),
) : ViewModel() {

    private var loadJob: Job? = null

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
            MealDiaryAction.RefreshRequested -> {
                loadDiary(_uiState.value.selectedDate, showLoading = false)
            }
            MealDiaryAction.FabToggled -> {
                _uiState.update { it.copy(isFabExpanded = !it.isFabExpanded) }
            }
            MealDiaryAction.FabCollapsed -> {
                _uiState.update { it.copy(isFabExpanded = false) }
            }
            MealDiaryAction.AddMealClicked -> {
                _uiState.update { it.copy(isFabExpanded = false) }
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToSearch(_uiState.value.selectedDate))
                }
            }
            MealDiaryAction.AddMealVoiceClicked -> {
                _uiState.update { it.copy(isFabExpanded = false) }
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToSearch(_uiState.value.selectedDate))
                }
            }
            MealDiaryAction.AddMealCameraClicked -> {
                _uiState.update { it.copy(isFabExpanded = false) }
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToMealCamera(_uiState.value.selectedDate))
                }
            }
            MealDiaryAction.OptionsMenuToggled -> {
                _uiState.update { it.copy(isOptionsMenuExpanded = !it.isOptionsMenuExpanded) }
            }
            MealDiaryAction.OptionsMenuDismissed -> {
                _uiState.update { it.copy(isOptionsMenuExpanded = false) }
            }
            MealDiaryAction.NutritionSettingsClicked -> {
                _uiState.update { it.copy(isOptionsMenuExpanded = false) }
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToNutritionSettings)
                }
            }
            MealDiaryAction.AppSettingsClicked -> {
                _uiState.update { it.copy(isOptionsMenuExpanded = false) }
                viewModelScope.launch {
                    _effect.emit(MealDiaryEffect.NavigateToAppSettings)
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
            runCatching { nutritionRepository.getMealDiary(date) }
                .onSuccess { snapshot ->
                    _uiState.value = snapshot.toUiState()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message?.takeIf(String::isNotBlank)
                                ?: "Meal diary could not be loaded.",
                        )
                    }
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
            vitaminHighlights = micronutrientsConsumed.vitaminHighlights(micronutrientTargets),
            mineralHighlights = micronutrientsConsumed.mineralHighlights(micronutrientTargets),
            nutritionReferenceStatus = nutritionReferenceStatus,
            sodiumConsumedMg = sodiumConsumedMg,
            sodiumLimitMg = sodiumLimitMg,
            meals = meals.latestTimeGroupsFirst(),
            isLoading = false,
            errorMessage = null,
            isFabExpanded = false, // Always collapse FAB after a diary reload
        )
    }

    private fun MicronutrientSnapshot.vitaminHighlights(
        targets: MicronutrientSnapshot,
    ): List<NutrientProgressUi> = lowestReported(
        "Vitamin A" to (vitaminAMcgRae to targets.vitaminAMcgRae),
        "Vitamin C" to (vitaminCMg to targets.vitaminCMg),
        "Vitamin D" to (vitaminDMcg to targets.vitaminDMcg),
        "Vitamin E" to (vitaminEMg to targets.vitaminEMg),
        "Vitamin K" to (vitaminKMcg to targets.vitaminKMcg),
        "Thiamin" to (thiaminMg to targets.thiaminMg),
        "Riboflavin" to (riboflavinMg to targets.riboflavinMg),
        "Niacin" to (niacinMg to targets.niacinMg),
        "Vitamin B6" to (vitaminB6Mg to targets.vitaminB6Mg),
        "Vitamin B12" to (vitaminB12Mcg to targets.vitaminB12Mcg),
        "Folate" to (folateMcg to targets.folateMcg),
    )

    private fun MicronutrientSnapshot.mineralHighlights(
        targets: MicronutrientSnapshot,
    ): List<NutrientProgressUi> = lowestReported(
        "Calcium" to (calciumMg to targets.calciumMg),
        "Iron" to (ironMg to targets.ironMg),
        "Magnesium" to (magnesiumMg to targets.magnesiumMg),
        "Potassium" to (potassiumMg to targets.potassiumMg),
        "Zinc" to (zincMg to targets.zincMg),
        "Selenium" to (seleniumMcg to targets.seleniumMcg),
        "Phosphorus" to (phosphorusMg to targets.phosphorusMg),
        "Copper" to (copperMg to targets.copperMg),
        "Manganese" to (manganeseMg to targets.manganeseMg),
        "Iodine" to (iodineMcg to targets.iodineMcg),
    )

    private fun lowestReported(
        vararg values: Pair<String, Pair<Double?, Double?>>,
    ): List<NutrientProgressUi> = values.mapNotNull { (label, amounts) ->
        val (consumed, target) = amounts
        if (consumed == null || target == null || target <= 0.0) null
        else NutrientProgressUi(label, (consumed / target).coerceAtLeast(0.0))
    }.sortedBy(NutrientProgressUi::progress).take(2)

    private fun weekDaysAround(date: LocalDate): List<LocalDate> {
        return (0..6).map { offset ->
            date.minusDays(6L - offset.toLong())
        }
    }
}

private fun List<NutritionMeal>.latestTimeGroupsFirst(): List<NutritionMeal> {
    return map { meal -> meal.copy(type = meal.timeBasedGroup()) }
        .groupBy(NutritionMeal::type)
        .values
        .map { group ->
            group.sortedByDescending { meal ->
                meal.loggedAtEpochMillis ?: Long.MIN_VALUE
            }
        }
        .sortedByDescending { group ->
            group.firstOrNull()?.loggedAtEpochMillis ?: Long.MIN_VALUE
        }
        .flatten()
}

private fun NutritionMeal.timeBasedGroup(): String {
    val hour = loggedAtEpochMillis
        ?.let(Instant::ofEpochMilli)
        ?.atZone(ZoneId.systemDefault())
        ?.hour
        ?: return type
    return when (hour) {
        in 5..10 -> "BREAKFAST"
        in 11..15 -> "LUNCH"
        in 16..18 -> "SNACKS"
        else -> "DINNER"
    }
}
