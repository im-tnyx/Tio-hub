package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.compose.runtime.Immutable
import com.tnyx.features.nutrition.domain.models.NutritionMeal

@Immutable
data class MealEditorUiState(
    val meal: NutritionMeal = NutritionMeal(id = "", name = "", type = "BREAKFAST"),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

sealed class MealEditorAction {
    data class NameChanged(val name: String) : MealEditorAction()
    data class CategoryChanged(val category: String) : MealEditorAction()
    data class ItemDeleted(val itemId: String) : MealEditorAction()
    data class ItemQuantityChanged(val itemId: String, val quantity: Double) : MealEditorAction()
    data object AddItemClicked : MealEditorAction()
    data object SaveClicked : MealEditorAction()
    data object BackClicked : MealEditorAction()
    data object ShareClicked : MealEditorAction()
    data object EditNameRequested : MealEditorAction()
}

sealed class MealEditorEffect {
    data object NavigateBack : MealEditorEffect()
    data class NavigateToItemEditor(val itemId: String) : MealEditorEffect()
    data object ShowShareOptions : MealEditorEffect()
    data object ShowNameEditDialog : MealEditorEffect()
}
