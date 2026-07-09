package com.tnyx.features.nutrition.presentation.targets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionTargetsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(NutritionTargetsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<NutritionTargetsEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: NutritionTargetsAction) {
        when (action) {
            NutritionTargetsAction.BackClicked -> emitEffect(NutritionTargetsEffect.NavigateBack)
            is NutritionTargetsAction.DynamicCaloriesChanged -> {
                _uiState.update { it.copy(dynamicCaloriesEnabled = action.enabled) }
            }
            is NutritionTargetsAction.EditTargetClicked -> {
                _uiState.update { state ->
                    state.copy(
                        activeEditField = action.field,
                        editValue = state.valueFor(action.field)
                    )
                }
            }
            is NutritionTargetsAction.EditValueChanged -> {
                _uiState.update { it.copy(editValue = action.value) }
            }
            NutritionTargetsAction.EditDismissed -> {
                _uiState.update { it.copy(activeEditField = null, editValue = "") }
            }
            NutritionTargetsAction.EditSaved -> saveEditedTarget()
            NutritionTargetsAction.RecalculateClicked -> recalculateTargets()
        }
    }

    private fun saveEditedTarget() {
        val state = _uiState.value
        val field = state.activeEditField ?: return
        val numericValue = state.editValue.trim().toDoubleOrNull()

        if (numericValue == null || numericValue <= 0.0) {
            emitEffect(NutritionTargetsEffect.ShowMessage("Enter a valid positive value."))
            return
        }

        _uiState.update {
            when (field) {
                NutritionTargetField.Calories -> it.copy(caloriesTarget = numericValue.toInt())
                NutritionTargetField.CalorieSurplus -> it.copy(calorieSurplusTarget = numericValue.toInt())
                NutritionTargetField.Protein -> it.copy(proteinTarget = numericValue)
                NutritionTargetField.Carbs -> it.copy(carbsTarget = numericValue)
                NutritionTargetField.Fat -> it.copy(fatTarget = numericValue)
                NutritionTargetField.Fiber -> it.copy(fiberTarget = numericValue)
                NutritionTargetField.Water -> it.copy(waterTargetLitres = numericValue)
                NutritionTargetField.GlassSize -> it.copy(glassSizeMl = numericValue.toInt())

                // नए फील्ड्स यहाँ जोड़े गए हैं:
                NutritionTargetField.Steps -> it.copy(stepsTarget = numericValue.toInt())
                NutritionTargetField.TargetWeight -> it.copy(targetWeight = numericValue)
                NutritionTargetField.SleepSchedule -> it.copy(sleepTargetHours = numericValue.toCleanString())
            }.copy(
                activeEditField = null,
                editValue = "",
                lastUpdatedLabel = "Updated just now"
            )
        }
        emitEffect(NutritionTargetsEffect.ShowMessage("${field.title} updated."))
    }

    private fun recalculateTargets() {
        _uiState.update {
            it.copy(
                dynamicCaloriesEnabled = true,
                caloriesTarget = 2580,
                calorieSurplusTarget = 250,
                proteinTarget = 145.0,
                carbsTarget = 275.0,
                fatTarget = 72.0,
                fiberTarget = 32.0,
                waterTargetLitres = 3.3,

                // अगर आप चाहें तो Recalculate पर नए फील्ड्स भी अपडेट कर सकते हैं
                stepsTarget = 10000,
                targetWeight = 70.0,
                sleepTargetHours = "8.0",

                lastUpdatedLabel = "Recalculated just now"
            )
        }
        emitEffect(NutritionTargetsEffect.ShowMessage("Targets recalculated."))
    }

    private fun emitEffect(effect: NutritionTargetsEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }

    private fun NutritionTargetsUiState.valueFor(field: NutritionTargetField): String =
        when (field) {
            NutritionTargetField.Calories -> caloriesTarget.toString()
            NutritionTargetField.CalorieSurplus -> calorieSurplusTarget.toString()
            NutritionTargetField.Protein -> proteinTarget.toCleanString()
            NutritionTargetField.Carbs -> carbsTarget.toCleanString()
            NutritionTargetField.Fat -> fatTarget.toCleanString()
            NutritionTargetField.Fiber -> fiberTarget.toCleanString()
            NutritionTargetField.Water -> waterTargetLitres.toCleanString()
            NutritionTargetField.GlassSize -> glassSizeMl.toString()

            // नए फील्ड्स यहाँ जोड़े गए हैं:
            NutritionTargetField.Steps -> stepsTarget.toString()
            NutritionTargetField.TargetWeight -> targetWeight.toCleanString()
            NutritionTargetField.SleepSchedule -> sleepTargetHours
        }

    private fun Double.toCleanString(): String =
        if (this % 1.0 == 0.0) toInt().toString() else toString()
}