package com.tnyx.features.nutrition.presentation.search

import androidx.compose.runtime.Immutable
import com.tnyx.features.nutrition.domain.models.MealItem
import java.time.LocalDate

@Immutable
data class MealSearchUiState(
    val query: String = "",
    val date: LocalDate = LocalDate.now(),
    val searchResults: List<MealItem> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val selectedGroup: MealSearchGroup? = null,
)

enum class MealSearchGroup {
    FoodDatabase,
}

sealed class MealSearchAction {
    data class QueryChanged(val query: String) : MealSearchAction()
    data object SearchSubmitted : MealSearchAction()
    data class GroupSelected(val group: MealSearchGroup) : MealSearchAction()
    data object GroupClosed : MealSearchAction()
    data class FoodItemSelected(val item: MealItem) : MealSearchAction()
    data object BackClicked : MealSearchAction()
}

sealed class MealSearchEffect {
    data object NavigateBack : MealSearchEffect()
    data class NavigateToMealEditor(val date: LocalDate, val initialItem: MealItem? = null) : MealSearchEffect()
}
