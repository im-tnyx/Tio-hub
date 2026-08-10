package com.tnyx.features.nutrition.presentation.meal_camera

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.repository.MealPhotoAnalysisException
import com.tnyx.features.nutrition.domain.repository.MealPhotoRecognitionRepository
import com.tnyx.features.nutrition.domain.repository.FoodSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MealCameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recognitionRepository: MealPhotoRecognitionRepository,
    private val foodSearchRepository: FoodSearchRepository,
) : ViewModel() {

    private val logDate = savedStateHandle.get<String>("date")
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now()

    private val _uiState = MutableStateFlow(MealCameraUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MealCameraEffect>()
    val effect = _effect.asSharedFlow()

    fun handleAction(action: MealCameraAction) {
        when (action) {
            is MealCameraAction.CameraPermissionChanged -> _uiState.update {
                it.copy(
                    hasCameraPermission = action.granted,
                    isPermissionResolved = true,
                    errorMessage = null,
                )
            }
            is MealCameraAction.CameraReady -> _uiState.update {
                it.copy(isCameraReady = true, hasFlash = action.hasFlash, errorMessage = null)
            }
            is MealCameraAction.CameraFailed -> _uiState.update {
                it.copy(isCameraReady = false, errorMessage = action.message)
            }
            MealCameraAction.FlashClicked -> _uiState.update {
                if (it.hasFlash) it.copy(isFlashEnabled = !it.isFlashEnabled) else it
            }
            MealCameraAction.BarcodeClicked -> _uiState.update {
                it.copy(isBarcodeMode = !it.isBarcodeMode, errorMessage = null)
            }
            is MealCameraAction.BarcodeDetected -> {
                resolveBarcode(action.value)
            }
            is MealCameraAction.PhotoCaptured -> _uiState.update {
                it.copy(
                    capturedPhotoPath = action.path,
                    capturedPhotoMimeType = action.mimeType,
                    isFlashEnabled = false,
                    isBarcodeMode = false,
                    errorMessage = null,
                )
            }
            is MealCameraAction.PhotoSelectionFailed -> _uiState.update {
                it.copy(errorMessage = action.message)
            }
            MealCameraAction.RetryClicked -> _uiState.update {
                it.copy(
                    capturedPhotoPath = null,
                    capturedPhotoMimeType = "image/jpeg",
                    isAnalyzing = false,
                    errorMessage = null,
                )
            }
            MealCameraAction.AnalysisPreparationStarted -> _uiState.update {
                it.copy(isAnalyzing = true, errorMessage = null)
            }
            is MealCameraAction.AnalysisPrepared -> analyze(action.imageBytes)
            is MealCameraAction.AnalysisPreparationFailed -> _uiState.update {
                it.copy(isAnalyzing = false, errorMessage = action.message)
            }
            MealCameraAction.BackClicked -> viewModelScope.launch {
                _effect.emit(MealCameraEffect.NavigateBack)
            }
        }
    }

    private fun resolveBarcode(barcode: String) {
        _uiState.update {
            it.copy(
                isBarcodeMode = false,
                isResolvingBarcode = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { foodSearchRepository.lookupBarcode(barcode) }
                .onSuccess { item ->
                    _uiState.update { it.copy(isResolvingBarcode = false) }
                    if (item != null) {
                        _effect.emit(MealCameraEffect.OpenBarcodeMealEditor(item))
                    } else {
                        _effect.emit(MealCameraEffect.OpenBarcodeSearch(barcode))
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isResolvingBarcode = false,
                            errorMessage = "Barcode lookup is unavailable. Try again.",
                        )
                    }
                }
        }
    }

    private fun analyze(imageBytes: ByteArray) {
        val state = _uiState.value
        val photoPath = state.capturedPhotoPath ?: return
        viewModelScope.launch {
            runCatching {
                recognitionRepository.analyze(
                    imageBytes = imageBytes,
                    mimeType = "image/jpeg",
                )
            }.onSuccess { analysis ->
                if (analysis.items.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = "No food was detected. Retry or choose another photo.",
                        )
                    }
                    return@onSuccess
                }
                val meal = NutritionMeal(
                    id = "",
                    name = analysis.suggestedName.ifBlank { "Photo meal" },
                    type = mealTypeFor(logDate),
                    items = analysis.items,
                    servingsDescription = analysis.items.firstOrNull()?.unit.orEmpty(),
                )
                _uiState.update { it.copy(isAnalyzing = false) }
                _effect.emit(
                    MealCameraEffect.OpenMealEditor(
                        meal = meal,
                        photoPath = photoPath,
                        photoMimeType = state.capturedPhotoMimeType,
                    )
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = if (error is MealPhotoAnalysisException) {
                            error.message?.takeIf(String::isNotBlank)
                                ?: "Meal photo could not be analyzed."
                        } else {
                            "Meal photo could not be analyzed."
                        },
                    )
                }
            }
        }
    }
}

private fun mealTypeFor(date: LocalDate): String {
    val hour = if (date == LocalDate.now()) java.time.LocalTime.now().hour else 12
    return when (hour) {
        in 5..10 -> "BREAKFAST"
        in 11..15 -> "LUNCH"
        in 16..18 -> "SNACK"
        else -> "DINNER"
    }
}
