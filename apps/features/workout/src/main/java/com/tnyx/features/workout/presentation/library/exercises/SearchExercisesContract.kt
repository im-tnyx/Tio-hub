package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.runtime.Immutable

@Immutable
data class SearchExercisesUiState(
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedFilter: String = "ALL",
    val isLoading: Boolean = false,
)

sealed interface SearchExercisesAction {
    data class QueryChanged(val query: String) : SearchExercisesAction
    data object SearchIconClicked : SearchExercisesAction
    data object FilterIconClicked : SearchExercisesAction
    data object CreateIconClicked : SearchExercisesAction
    data object BackClicked : SearchExercisesAction
}
