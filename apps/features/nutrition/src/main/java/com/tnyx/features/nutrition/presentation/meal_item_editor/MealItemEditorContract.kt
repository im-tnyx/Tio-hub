package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.compose.runtime.Immutable
import com.tnyx.features.nutrition.domain.models.MealItem

@Immutable
data class MealItemEditorUiState(
    val item: MealItem = MealItem(id = "", name = "", calories = 0, protein = 0.0, quantity = 1.0, unit = "piece"),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNameEditorVisible: Boolean = false,
    val nameInput: String = "",
    val nameEditorError: String? = null,
    val isMicronutrientsExpanded: Boolean = false,
    val errorMessage: String? = null,
)

sealed class MealItemEditorAction {
    data object NameEditorRequested : MealItemEditorAction()
    data class NameEditorInputChanged(val name: String) : MealItemEditorAction()
    data object NameEditorConfirmed : MealItemEditorAction()
    data object NameEditorDismissed : MealItemEditorAction()
    data class QuantityChanged(val quantity: Double) : MealItemEditorAction()
    data class UnitChanged(val unit: String) : MealItemEditorAction()
    data class NutrientChanged(val field: String, val value: Double) : MealItemEditorAction()
    data object MicronutrientsToggled : MealItemEditorAction()
    data object ResetClicked : MealItemEditorAction()
    data object SaveClicked : MealItemEditorAction()
    data object RemoveClicked : MealItemEditorAction()
    data object BackClicked : MealItemEditorAction()
}

sealed class MealItemEditorEffect {
    data object NavigateBack : MealItemEditorEffect()
    data class ItemSaved(val item: MealItem) : MealItemEditorEffect()
    data class ItemRemoved(val itemId: String) : MealItemEditorEffect()
}
