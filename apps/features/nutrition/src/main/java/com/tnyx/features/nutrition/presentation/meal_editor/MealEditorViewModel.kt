package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(MealEditorUiState(
        meal = NutritionMeal(
            id = savedStateHandle.get<String>("mealId").orEmpty(),
            name = "",
            type = "BREAKFAST",
            items = emptyList(),
            description = "",
        ),
    ))
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
            is MealEditorAction.ItemDeleted -> {
                _uiState.update { it.copy(meal = it.meal.copy(items = it.meal.items.filter { item -> item.id != action.itemId })) }
            }
            is MealEditorAction.ItemQuantityChanged -> {
                _uiState.update { 
                    it.copy(meal = it.meal.copy(items = it.meal.items.map { item ->
                        if (item.id == action.itemId) item.copy(quantity = action.quantity) else item
                    }))
                }
            }
            MealEditorAction.AddItemClicked -> {
                // Navigate to search or add item screen
            }
            MealEditorAction.SaveClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true) }
                    // Simulate API call
                    _effect.emit(MealEditorEffect.NavigateBack)
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
