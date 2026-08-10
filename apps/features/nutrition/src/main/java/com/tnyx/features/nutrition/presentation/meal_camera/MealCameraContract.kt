package com.tnyx.features.nutrition.presentation.meal_camera

import androidx.compose.runtime.Immutable
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.MealItem

@Immutable
data class MealCameraUiState(
    val hasCameraPermission: Boolean = false,
    val isPermissionResolved: Boolean = false,
    val isCameraReady: Boolean = false,
    val hasFlash: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val isBarcodeMode: Boolean = false,
    val isResolvingBarcode: Boolean = false,
    val capturedPhotoPath: String? = null,
    val capturedPhotoMimeType: String = "image/jpeg",
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MealCameraAction {
    data class CameraPermissionChanged(val granted: Boolean) : MealCameraAction
    data class CameraReady(val hasFlash: Boolean) : MealCameraAction
    data class CameraFailed(val message: String) : MealCameraAction
    data object FlashClicked : MealCameraAction
    data object BarcodeClicked : MealCameraAction
    data class BarcodeDetected(val value: String) : MealCameraAction
    data class PhotoCaptured(
        val path: String,
        val mimeType: String,
    ) : MealCameraAction
    data class PhotoSelectionFailed(val message: String) : MealCameraAction
    data object RetryClicked : MealCameraAction
    data object AnalysisPreparationStarted : MealCameraAction
    data class AnalysisPrepared(val imageBytes: ByteArray) : MealCameraAction
    data class AnalysisPreparationFailed(val message: String) : MealCameraAction
    data object BackClicked : MealCameraAction
}

sealed interface MealCameraEffect {
    data object NavigateBack : MealCameraEffect
    data class OpenBarcodeSearch(val barcode: String) : MealCameraEffect
    data class OpenBarcodeMealEditor(val item: MealItem) : MealCameraEffect
    data class OpenMealEditor(
        val meal: NutritionMeal,
        val photoPath: String,
        val photoMimeType: String,
    ) : MealCameraEffect
}
