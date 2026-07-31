package com.tnyx.features.workout.presentation.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier

@Composable
fun ExerciseLibraryRoute(
    onNavigateBack: () -> Unit,
    onExerciseSelected: (String) -> Unit,
    onCreateExercise: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ExerciseLibraryScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                ExerciseLibraryAction.BackClicked -> onNavigateBack()
                is ExerciseLibraryAction.ExerciseClicked -> onExerciseSelected(action.exerciseId)
                ExerciseLibraryAction.CreateExerciseClicked -> onCreateExercise()
                else -> viewModel.handleAction(action)
            }
        },
        modifier = modifier
    )
}
