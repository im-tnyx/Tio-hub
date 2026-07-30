package com.tnyx.features.nutrition.presentation.targets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NutritionTargetsViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NutritionTargetsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<NutritionTargetsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadTargets()
    }

    fun handleAction(action: NutritionTargetsAction) {
        when (action) {
            NutritionTargetsAction.BackClicked -> emitEffect(NutritionTargetsEffect.NavigateBack)
            is NutritionTargetsAction.DynamicCaloriesChanged -> {
                persistUpdatedState(
                    updatedState = _uiState.value.copy(dynamicCaloriesEnabled = action.enabled),
                    successMessage = if (action.enabled) {
                        "Dynamic calories enabled."
                    } else {
                        "Dynamic calories disabled."
                    },
                    successLabel = "Updated just now",
                )
            }
            is NutritionTargetsAction.EditTargetClicked -> {
                _uiState.update { state ->
                    state.copy(
                        activeEditField = action.field,
                        editValue = state.valueFor(action.field),
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

    private fun loadTargets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { nutritionRepository.getNutritionTargets() }
                .onSuccess { targets ->
                    _uiState.value = targets.toUiState()
                }
                .onFailure {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            lastUpdatedLabel = "Sync failed",
                        )
                    }
                    _effect.emit(
                        NutritionTargetsEffect.ShowMessage(
                            "Nutrition targets could not be loaded.",
                        ),
                    )
                }
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

        val updatedState = state.let {
            when (field) {
                NutritionTargetField.Calories -> it.copy(caloriesTarget = numericValue.toInt())
                NutritionTargetField.CalorieSurplus -> it.copy(calorieSurplusTarget = numericValue.toInt())
                NutritionTargetField.Protein -> it.copy(proteinTarget = numericValue)
                NutritionTargetField.Carbs -> it.copy(carbsTarget = numericValue)
                NutritionTargetField.Fat -> it.copy(fatTarget = numericValue)
                NutritionTargetField.Fiber -> it.copy(fiberTarget = numericValue)
                NutritionTargetField.Water -> it.copy(waterTargetLitres = numericValue)
                NutritionTargetField.GlassSize -> it.copy(glassSizeMl = numericValue.toInt())
                NutritionTargetField.Steps -> it.copy(stepsTarget = numericValue.toInt())
                NutritionTargetField.TargetWeight -> it.copy(targetWeight = numericValue)
                NutritionTargetField.SleepSchedule -> {
                    val sleepTargetHours = numericValue.toCleanString()
                    it.copy(
                        sleepTargetHours = sleepTargetHours,
                        formattedWakeTime = it.recalculateWakeTime(sleepTargetHours),
                    )
                }
            }
        }.copy(
            activeEditField = null,
            editValue = "",
        )

        persistUpdatedState(
            updatedState = updatedState,
            successMessage = "${field.title} updated.",
            successLabel = "Updated just now",
        )
    }

    private fun recalculateTargets() {
        val currentState = _uiState.value
        val calculatedCalories = (
            currentState.proteinTarget * 4.0 +
                currentState.carbsTarget * 4.0 +
                currentState.fatTarget * 9.0
            ).roundToInt()

        if (calculatedCalories <= 0) {
            emitEffect(
                NutritionTargetsEffect.ShowMessage(
                    "Set macros first to recalculate calories.",
                ),
            )
            return
        }

        persistUpdatedState(
            updatedState = currentState.copy(
                dynamicCaloriesEnabled = true,
                caloriesTarget = calculatedCalories,
            ),
            successMessage = "Targets recalculated.",
            successLabel = "Recalculated just now",
        )
    }

    private fun persistUpdatedState(
        updatedState: NutritionTargetsUiState,
        successMessage: String,
        successLabel: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                nutritionRepository.updateNutritionTargets(updatedState.toSnapshot())
            }.onSuccess {
                _uiState.value = updatedState.copy(
                    isLoading = false,
                    isSaving = false,
                    lastUpdatedLabel = successLabel,
                )
                _effect.emit(NutritionTargetsEffect.ShowMessage(successMessage))
            }.onFailure {
                _uiState.update { currentState ->
                    currentState.copy(isSaving = false)
                }
                _effect.emit(
                    NutritionTargetsEffect.ShowMessage(
                        "Nutrition targets could not be updated.",
                    ),
                )
            }
        }
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
            NutritionTargetField.Steps -> stepsTarget.toString()
            NutritionTargetField.TargetWeight -> targetWeight.toCleanString()
            NutritionTargetField.SleepSchedule -> sleepTargetHours
        }

    private fun NutritionTargetsSnapshot.toUiState(): NutritionTargetsUiState {
        return NutritionTargetsUiState(
            isLoading = false,
            isSaving = false,
            dynamicCaloriesEnabled = dynamicCaloriesEnabled,
            caloriesTarget = caloriesTarget,
            calorieSurplusTarget = calorieSurplusTarget,
            proteinTarget = proteinTarget,
            carbsTarget = carbsTarget,
            fatTarget = fatTarget,
            fiberTarget = fiberTarget,
            waterTargetLitres = waterTargetLitres,
            glassSizeMl = glassSizeMl,
            lastUpdatedLabel = "Synced",
            stepsTarget = stepsTarget,
            targetWeight = targetWeight,
            sleepTargetHours = sleepTargetHours,
            formattedSleepTime = formattedSleepTime,
            formattedWakeTime = formattedWakeTime,
        )
    }

    private fun NutritionTargetsUiState.toSnapshot(): NutritionTargetsSnapshot {
        return NutritionTargetsSnapshot(
            dynamicCaloriesEnabled = dynamicCaloriesEnabled,
            caloriesTarget = caloriesTarget,
            calorieSurplusTarget = calorieSurplusTarget,
            proteinTarget = proteinTarget,
            carbsTarget = carbsTarget,
            fatTarget = fatTarget,
            fiberTarget = fiberTarget,
            waterTargetLitres = waterTargetLitres,
            glassSizeMl = glassSizeMl,
            stepsTarget = stepsTarget,
            targetWeight = targetWeight,
            sleepTargetHours = sleepTargetHours,
            formattedSleepTime = formattedSleepTime,
            formattedWakeTime = formattedWakeTime,
        )
    }

    private fun NutritionTargetsUiState.recalculateWakeTime(updatedSleepHours: String): String {
        val bedTime = parseUiTime(formattedSleepTime) ?: return formattedWakeTime
        val duration = updatedSleepHours.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return formattedWakeTime
        return bedTime
            .plusMinutes((duration * 60.0).roundToInt().toLong())
            .format(uiTimeFormatter)
    }

    private fun parseUiTime(value: String): LocalTime? {
        val normalized = value.trim()
        if (normalized.isBlank()) return null
        return runCatching { LocalTime.parse(normalized, uiTimeFormatter) }.getOrNull()
    }

    private fun Double.toCleanString(): String {
        return if (this % 1.0 == 0.0) {
            toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
        }
    }

    private companion object {
        val uiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    }
}
