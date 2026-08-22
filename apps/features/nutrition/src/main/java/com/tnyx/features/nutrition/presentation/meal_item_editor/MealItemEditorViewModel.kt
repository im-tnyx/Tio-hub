package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MicronutrientSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@HiltViewModel
class MealItemEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val blankItem = MealItem(
        id = "",
        name = "",
        calories = 0,
        protein = 0.0,
        quantity = 1.0,
        unit = "serving",
    )

    private val initialItem = savedStateHandle.get<String>("itemJson")
        ?.let { serialized ->
            runCatching { Json.decodeFromString<MealItem>(serialized) }.getOrNull()
        }

    private val _uiState = MutableStateFlow(
        MealItemEditorUiState(
            item = initialItem ?: blankItem,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealItemEditorEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: MealItemEditorAction) {
        when (action) {
            MealItemEditorAction.NameEditorRequested -> {
                _uiState.update {
                    it.copy(
                        isNameEditorVisible = true,
                        nameInput = it.item.name,
                        nameEditorError = null,
                    )
                }
            }
            is MealItemEditorAction.NameEditorInputChanged -> {
                _uiState.update { it.copy(nameInput = action.name, nameEditorError = null) }
            }
            MealItemEditorAction.NameEditorConfirmed -> confirmNameEditor()
            MealItemEditorAction.NameEditorDismissed -> {
                _uiState.update {
                    it.copy(isNameEditorVisible = false, nameEditorError = null)
                }
            }
            is MealItemEditorAction.QuantityChanged -> {
                _uiState.update {
                    it.copy(item = it.item.copy(quantity = action.quantity), errorMessage = null)
                }
            }
            is MealItemEditorAction.UnitChanged -> {
                _uiState.update {
                    it.copy(item = it.item.copy(unit = action.unit), errorMessage = null)
                }
            }
            is MealItemEditorAction.NutrientChanged -> updateNutrient(action.field, action.value)
            is MealItemEditorAction.MicronutrientChanged -> {
                updateMicronutrient(action.field, action.value)
            }
            MealItemEditorAction.MicronutrientsToggled -> {
                _uiState.update {
                    it.copy(isMicronutrientsExpanded = !it.isMicronutrientsExpanded)
                }
            }
            MealItemEditorAction.ResetClicked -> {
                _uiState.update {
                    it.copy(
                        item = initialItem ?: blankItem,
                        isNameEditorVisible = false,
                        nameEditorError = null,
                        errorMessage = null,
                    )
                }
            }
            MealItemEditorAction.SaveClicked -> saveItem()
            MealItemEditorAction.RemoveClicked -> {
                viewModelScope.launch {
                    if (initialItem != null) {
                        _effect.emit(MealItemEditorEffect.ItemRemoved(initialItem.id))
                    } else {
                        _effect.emit(MealItemEditorEffect.NavigateBack)
                    }
                }
            }
            MealItemEditorAction.BackClicked -> {
                viewModelScope.launch { _effect.emit(MealItemEditorEffect.NavigateBack) }
            }
        }
    }

    private fun confirmNameEditor() {
        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameEditorError = "Enter a food item name.") }
            return
        }
        _uiState.update {
            it.copy(
                item = it.item.copy(name = name),
                isNameEditorVisible = false,
                nameEditorError = null,
                errorMessage = null,
            )
        }
    }

    private fun saveItem() {
        val item = _uiState.value.item
        val validationError = validateItem(item)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val resolvedItem = item.copy(
            id = item.id.ifBlank { UUID.randomUUID().toString() },
            name = item.name.trim(),
            unit = item.unit.trim(),
        )
        viewModelScope.launch {
            _effect.emit(MealItemEditorEffect.ItemSaved(resolvedItem))
        }
    }

    private fun updateNutrient(field: String, value: Double) {
        _uiState.update { state ->
            val item = when (field) {
                "calories" -> state.item.copy(calories = value.toInt())
                "protein" -> state.item.copy(protein = value)
                "carbs" -> state.item.copy(carbs = value)
                "fats" -> state.item.copy(fats = value)
                "fiber" -> state.item.copy(fiber = value)
                "sugar" -> state.item.copy(sugar = value)
                "transFat" -> state.item.copy(transFat = value)
                "saturatedFat" -> state.item.copy(saturatedFat = value)
                else -> state.item
            }
            state.copy(item = item, errorMessage = null)
        }
    }

    private fun updateMicronutrient(field: String, value: Double?) {
        _uiState.update { state ->
            state.copy(
                item = state.item.copy(
                    micronutrients = state.item.micronutrients.withValue(field, value),
                ),
                errorMessage = null,
            )
        }
    }

    private fun validateItem(item: MealItem): String? {
        if (item.name.isBlank()) return "Enter a food item name."
        if (item.quantity <= 0.0) return "Quantity must be greater than zero."
        if (item.unit.isBlank()) return "Enter a unit."
        val nutrients = listOf(
            item.calories.toDouble(),
            item.protein,
            item.carbs,
            item.fats,
            item.fiber,
            item.sugar,
            item.transFat,
            item.saturatedFat,
        )
        if (nutrients.any { it < 0.0 }) return "Nutrition values cannot be negative."
        return null
    }
}

private fun MicronutrientSnapshot.withValue(
    field: String,
    value: Double?,
): MicronutrientSnapshot = when (field) {
    "vitaminAMcgRae" -> copy(vitaminAMcgRae = value)
    "vitaminCMg" -> copy(vitaminCMg = value)
    "vitaminDMcg" -> copy(vitaminDMcg = value)
    "vitaminEMg" -> copy(vitaminEMg = value)
    "vitaminKMcg" -> copy(vitaminKMcg = value)
    "thiaminMg" -> copy(thiaminMg = value)
    "riboflavinMg" -> copy(riboflavinMg = value)
    "niacinMg" -> copy(niacinMg = value)
    "vitaminB6Mg" -> copy(vitaminB6Mg = value)
    "vitaminB12Mcg" -> copy(vitaminB12Mcg = value)
    "folateMcg" -> copy(folateMcg = value)
    "calciumMg" -> copy(calciumMg = value)
    "ironMg" -> copy(ironMg = value)
    "magnesiumMg" -> copy(magnesiumMg = value)
    "potassiumMg" -> copy(potassiumMg = value)
    "zincMg" -> copy(zincMg = value)
    "seleniumMcg" -> copy(seleniumMcg = value)
    "phosphorusMg" -> copy(phosphorusMg = value)
    "copperMg" -> copy(copperMg = value)
    "manganeseMg" -> copy(manganeseMg = value)
    "iodineMcg" -> copy(iodineMcg = value)
    else -> this
}
