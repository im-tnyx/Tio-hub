package com.tnyx.features.workout.presentation.library.exerciseinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExerciseInfoRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseInfoScreen(
        state = state,
        onAction = { action ->
            if (action is ExerciseInfoAction.BackClicked) {
                onNavigateBack()
            } else {
                viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}
