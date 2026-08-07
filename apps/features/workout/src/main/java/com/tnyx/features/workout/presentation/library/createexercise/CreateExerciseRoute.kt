package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreateExerciseRoute(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                CreateExerciseEvent.SaveSuccess -> onSaveSuccess()
                is CreateExerciseEvent.SaveError -> {
                    // Handled if needed
                }
            }
        }
    }

    CreateExerciseScreen(
        state = uiState,
        onAction = { action ->
            if (action is CreateExerciseAction.BackClicked) {
                onNavigateBack()
            } else {
                viewModel.onAction(action)
            }
        },
        modifier = modifier
    )
}
