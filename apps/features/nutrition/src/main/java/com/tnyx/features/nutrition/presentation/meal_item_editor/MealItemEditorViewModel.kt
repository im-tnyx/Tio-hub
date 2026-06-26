package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealItemEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealItemEditorUiState(
        item = MealItem(id = "i1", name = "Whole Grain Bread", calories = 120, protein = 4.0, quantity = 2.0, unit = "slice")
    ))
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealItemEditorEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: MealItemEditorAction) {
        when (action) {
            is MealItemEditorAction.NameChanged -> {
                _uiState.update { it.copy(item = it.item.copy(name = action.name)) }
            }
            is MealItemEditorAction.QuantityChanged -> {
                _uiState.update { it.copy(item = it.item.copy(quantity = action.quantity)) }
            }
            is MealItemEditorAction.UnitChanged -> {
                _uiState.update { it.copy(item = it.item.copy(unit = action.unit)) }
            }
            is MealItemEditorAction.NutrientChanged -> {
                updateNutrient(action.field, action.value)
            }
            MealItemEditorAction.SaveClicked -> {
                viewModelScope.launch { _effect.emit(MealItemEditorEffect.NavigateBack) }
            }
            MealItemEditorAction.RemoveClicked -> {
                viewModelScope.launch { _effect.emit(MealItemEditorEffect.NavigateBack) }
            }
            MealItemEditorAction.BackClicked -> {
                viewModelScope.launch { _effect.emit(MealItemEditorEffect.NavigateBack) }
            }
        }
    }

    private fun updateNutrient(field: String, value: Double) {
        _uiState.update { state ->
            val newItem = when (field) {
                "calories" -> state.item.copy(calories = value.toInt())
                "protein" -> state.item.copy(protein = value)
                "carbs" -> state.item.copy(carbs = value)
                "fats" -> state.item.copy(fats = value)
                "fiber" -> state.item.copy(fiber = value)
                "sugar" -> state.item.copy(sugar = value)
                else -> state.item
            }
            state.copy(item = newItem)
        }
    }
}
