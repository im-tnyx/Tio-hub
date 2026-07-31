package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import com.tnyx.features.nutrition.navigation.NutritionScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealItemEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nutritionRepository: NutritionRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NutritionScreen.MealItemEditor>()
    private val itemId = route.itemId

    // Blank form for new items; real item data loaded via future search/catalog
    private val _uiState = MutableStateFlow(
        MealItemEditorUiState(
            item = MealItem(
                id = itemId,
                name = "",
                calories = 0,
                protein = 0.0,
                quantity = 1.0,
                unit = "serving",
            )
        )
    )
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
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true) }
                    runCatching {
                        if (itemId.isBlank()) {
                            // New item — mealLogId caller side se aana chahiye (future: pass via route)
                            nutritionRepository.saveMealLogItem("", _uiState.value.item)
                        } else {
                            nutritionRepository.updateMealLogItem(_uiState.value.item)
                        }
                    }
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.emit(MealItemEditorEffect.NavigateBack)
                }
            }
            MealItemEditorAction.RemoveClicked -> {
                viewModelScope.launch {
                    if (itemId.isNotBlank()) {
                        runCatching { nutritionRepository.deleteMealLogItem(itemId) }
                    }
                    _effect.emit(MealItemEditorEffect.NavigateBack)
                }
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
