package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchExercisesRoute(
    onNavigateBack: () -> Unit,
    onExerciseInfoClick: (String) -> Unit = {},
    onExerciseSelect: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SearchExercisesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchExercisesScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
            when (action) {
                SearchExercisesAction.BackClicked -> onNavigateBack()
                SearchExercisesAction.FilterIconClicked -> onFilterClick()
                SearchExercisesAction.CreateIconClicked -> onCreateClick()
                is SearchExercisesAction.ExerciseInfoClicked -> onExerciseInfoClick(action.exerciseId)
                is SearchExercisesAction.ExerciseSelected -> onExerciseSelect(action.exerciseId)
                else -> {}
            }
        },
        modifier = modifier
    )
}
