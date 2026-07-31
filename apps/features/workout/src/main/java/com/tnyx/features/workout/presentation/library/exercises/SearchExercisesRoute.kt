package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SearchExercisesRoute(
    onNavigateBack: () -> Unit,
    onFilterClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf("ALL") }

    SearchExercisesScreen(
        state = SearchExercisesUiState(
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            selectedFilter = selectedFilter,
        ),
        onAction = { action ->
            when (action) {
                SearchExercisesAction.BackClicked -> onNavigateBack()
                SearchExercisesAction.SearchIconClicked -> {
                    isSearchActive = true
                }
                SearchExercisesAction.SearchModeDismissed -> {
                    isSearchActive = false
                    searchQuery = ""
                }
                SearchExercisesAction.FilterIconClicked -> onFilterClick()
                SearchExercisesAction.CreateIconClicked -> onCreateClick()
                is SearchExercisesAction.QueryChanged -> {
                    searchQuery = action.query
                }
            }
        },
        modifier = modifier
    )
}
