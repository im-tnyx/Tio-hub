package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SearchExercisesRoute(
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var uiState by remember { mutableStateOf(SearchExercisesUiState()) }

    SearchExercisesScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                SearchExercisesAction.BackClicked -> onNavigateBack()
                SearchExercisesAction.SearchIconClicked -> onSearchClick()
                SearchExercisesAction.FilterIconClicked -> onFilterClick()
                SearchExercisesAction.CreateIconClicked -> onCreateClick()
                is SearchExercisesAction.QueryChanged -> {
                    uiState = uiState.copy(searchQuery = action.query)
                }
            }
        },
        modifier = modifier
    )
}
