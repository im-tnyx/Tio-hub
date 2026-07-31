package com.tnyx.features.workout.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WorkoutRoute(
    onOpenHistory: () -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    WorkoutScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                WorkoutAction.HistoryClicked -> onOpenHistory()
                WorkoutAction.LibraryClicked -> onOpenLibrary()
                else -> viewModel.handleAction(action)
            }
        },
    )
}

@Composable
fun WorkoutHistoryRoute(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    WorkoutHistoryScreen(
        state = uiState,
        onAction = { action ->
            if (action == WorkoutAction.BackClicked) {
                onNavigateBack()
            } else {
                viewModel.handleAction(action)
            }
        },
    )
}
