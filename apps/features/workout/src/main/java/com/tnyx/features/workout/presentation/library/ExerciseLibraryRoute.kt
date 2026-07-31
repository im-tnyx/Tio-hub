package com.tnyx.features.workout.presentation.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExerciseLibraryRoute(
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateProgramClick: () -> Unit,
    onCreateRoutineClick: () -> Unit,
    onCreateExerciseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExerciseLibraryScreen(
        onSearchClick = onSearchClick,
        onCreateProgramClick = onCreateProgramClick,
        onCreateRoutineClick = onCreateRoutineClick,
        onCreateExerciseClick = onCreateExerciseClick,
        modifier = modifier
    )
}
