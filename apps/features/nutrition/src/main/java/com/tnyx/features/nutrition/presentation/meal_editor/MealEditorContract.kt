package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.compose.runtime.Immutable
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import java.time.LocalDate
import java.time.LocalDateTime

@Immutable
data class MealEditorUiState(
    val meal: NutritionMeal = NutritionMeal(id = "", name = "", type = "BREAKFAST"),
    val logDateTime: LocalDateTime = LocalDateTime.now(),
    val isLogDatePickerVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNameEditorVisible: Boolean = false,
    val nameInput: String = "",
    val nameEditorError: String? = null,
    val isServingCountEditorVisible: Boolean = false,
    val servingCountInput: String = "1",
    val servingCountError: String? = null,
    val isServingEditorVisible: Boolean = false,
    val servingAmountInput: String = "1",
    val servingUnitInput: String = "serving",
    val servingUnitOptions: List<String> = emptyList(),
    val servingEditorError: String? = null,
    val isPhotoSourceVisible: Boolean = false,
    val photoPreviewBytes: ByteArray? = null,
    val errorMessage: String? = null,
)

sealed class MealEditorAction {
    data class NameChanged(val name: String) : MealEditorAction()
    data class NameEditorInputChanged(val name: String) : MealEditorAction()
    data object NameEditorConfirmed : MealEditorAction()
    data object NameEditorDismissed : MealEditorAction()
    data object ServingCountEditorRequested : MealEditorAction()
    data class ServingCountChanged(val count: String) : MealEditorAction()
    data object ServingCountEditorConfirmed : MealEditorAction()
    data object ServingCountEditorDismissed : MealEditorAction()
    data object ServingEditorRequested : MealEditorAction()
    data class ServingAmountChanged(val amount: String) : MealEditorAction()
    data class ServingUnitSelected(val unit: String) : MealEditorAction()
    data object ServingEditorConfirmed : MealEditorAction()
    data object ServingEditorDismissed : MealEditorAction()
    data object PhotoClicked : MealEditorAction()
    data object PhotoSourceDismissed : MealEditorAction()
    data object CameraClicked : MealEditorAction()
    data object GalleryClicked : MealEditorAction()
    data class PhotoSelected(val bytes: ByteArray, val mimeType: String) : MealEditorAction()
    data class PhotoSelectionFailed(val message: String) : MealEditorAction()
    data object PhotoRemoved : MealEditorAction()
    data class CategoryChanged(val category: String) : MealEditorAction()
    data object LogDatePickerRequested : MealEditorAction()
    data class LogDateTimeChanged(val dateTime: LocalDateTime) : MealEditorAction()
    data object LogDatePickerDismissed : MealEditorAction()
    data class ItemDeleted(val itemId: String) : MealEditorAction()
    data class ItemClicked(val item: MealItem) : MealEditorAction()
    data class ItemUpserted(val item: MealItem) : MealEditorAction()
    data class ItemQuantityChanged(val itemId: String, val quantity: Double) : MealEditorAction()
    data object AddItemClicked : MealEditorAction()
    data object SaveClicked : MealEditorAction()
    data object DeleteMealClicked : MealEditorAction()
    data object BackClicked : MealEditorAction()
    data object ShareClicked : MealEditorAction()
    data object EditNameRequested : MealEditorAction()
}

sealed class MealEditorEffect {
    data object NavigateBack : MealEditorEffect()
    data object MealSaved : MealEditorEffect()
    data object MealDeleted : MealEditorEffect()
    data class NavigateToSearch(val date: LocalDate) : MealEditorEffect()
    data class NavigateToItemEditor(val item: MealItem?) : MealEditorEffect()
    data object ShowShareOptions : MealEditorEffect()
    data object LaunchCamera : MealEditorEffect()
    data object LaunchGallery : MealEditorEffect()
}
