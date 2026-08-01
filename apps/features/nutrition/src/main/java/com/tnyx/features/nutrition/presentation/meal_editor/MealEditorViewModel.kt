package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import com.tnyx.features.nutrition.navigation.NutritionScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MealEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nutritionRepository: NutritionRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NutritionScreen.MealEditor>()
    private val mealId = route.mealId
    private val logDate: LocalDate = route.date
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()

    private val _uiState = MutableStateFlow(
        MealEditorUiState(
            meal = NutritionMeal(
                id = mealId.orEmpty(),
                name = "",
                type = "BREAKFAST",
                items = emptyList(),
                description = "",
            ),
            logDateTime = logDate.atTime(LocalDateTime.now().toLocalTime()),
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealEditorEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: MealEditorAction) {
        when (action) {
            is MealEditorAction.NameChanged -> {
                _uiState.update { it.copy(meal = it.meal.copy(name = action.name)) }
            }
            is MealEditorAction.CategoryChanged -> {
                _uiState.update { it.copy(meal = it.meal.copy(type = action.category)) }
            }
            MealEditorAction.LogDatePickerRequested -> {
                _uiState.update { it.copy(isLogDatePickerVisible = true) }
            }
            is MealEditorAction.LogDateTimeChanged -> {
                _uiState.update { it.copy(logDateTime = action.dateTime) }
            }
            MealEditorAction.LogDatePickerDismissed -> {
                _uiState.update { it.copy(isLogDatePickerVisible = false) }
            }
            is MealEditorAction.ItemDeleted -> {
                val itemId = action.itemId
                _uiState.update {
                    it.copy(meal = it.meal.copy(items = it.meal.items.filter { item -> item.id != itemId }))
                }
                viewModelScope.launch {
                    runCatching { nutritionRepository.deleteMealLogItem(itemId) }
                }
            }
            is MealEditorAction.ItemQuantityChanged -> {
                _uiState.update {
                    it.copy(meal = it.meal.copy(items = it.meal.items.map { item ->
                        if (item.id == action.itemId) item.copy(quantity = action.quantity) else item
                    }))
                }
            }
            MealEditorAction.AddItemClicked -> {
                viewModelScope.launch {
                    _effect.emit(MealEditorEffect.NavigateToItemEditor(""))
                }
            }
            MealEditorAction.SaveClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true) }
                    runCatching {
                        nutritionRepository.saveMealLog(_uiState.value.logDateTime.toLocalDate(), _uiState.value.meal)
                    }
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.emit(MealEditorEffect.NavigateBack)
                }
            }
            MealEditorAction.DeleteMealClicked -> {
                val id = _uiState.value.meal.id
                if (id.isNotBlank()) {
                    viewModelScope.launch {
                        runCatching { nutritionRepository.deleteMealLog(id) }
                        _effect.emit(MealEditorEffect.NavigateBack)
                    }
                } else {
                    viewModelScope.launch { _effect.emit(MealEditorEffect.NavigateBack) }
                }
            }
            MealEditorAction.BackClicked -> {
                viewModelScope.launch { _effect.emit(MealEditorEffect.NavigateBack) }
            }
            MealEditorAction.ShareClicked -> {
                viewModelScope.launch { _effect.emit(MealEditorEffect.ShowShareOptions) }
            }
            MealEditorAction.EditNameRequested -> {
                viewModelScope.launch { _effect.emit(MealEditorEffect.ShowNameEditDialog) }
            }
        }
    }
}
