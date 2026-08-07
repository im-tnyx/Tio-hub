package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun CreateExerciseRoute(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var uiState by remember { mutableStateOf(CreateExerciseUiState()) }

    CreateExerciseScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                CreateExerciseAction.BackClicked -> onNavigateBack()
                CreateExerciseAction.SaveClicked -> {
                    onSaveSuccess()
                }
                is CreateExerciseAction.NameChanged -> {
                    uiState = uiState.copy(exerciseName = action.name)
                }
                is CreateExerciseAction.InstructionsChanged -> {
                    uiState = uiState.copy(instructions = action.instructions)
                }
                CreateExerciseAction.AddAssetClicked -> {
                    // Open asset picker
                }
                CreateExerciseAction.EquipmentClicked -> {
                    // Select equipment
                }
                CreateExerciseAction.BodyPartClicked -> {
                    // Select body part
                }
                CreateExerciseAction.PrimaryMuscleClicked -> {
                    uiState = uiState.copy(showPrimaryMuscleBottomSheet = true)
                }
                CreateExerciseAction.OtherMusclesClicked -> {
                    uiState = uiState.copy(showOtherMusclesBottomSheet = true)
                }
                is CreateExerciseAction.PrimaryMuscleSelected -> {
                    uiState = uiState.copy(
                        primaryMuscleGroup = action.muscle,
                        showPrimaryMuscleBottomSheet = false
                    )
                }
                is CreateExerciseAction.OtherMusclesSelected -> {
                    uiState = uiState.copy(
                        otherMuscles = action.muscle,
                        showOtherMusclesBottomSheet = false
                    )
                }
                CreateExerciseAction.PrimaryMuscleBottomSheetDismissed -> {
                    uiState = uiState.copy(showPrimaryMuscleBottomSheet = false)
                }
                CreateExerciseAction.OtherMusclesBottomSheetDismissed -> {
                    uiState = uiState.copy(showOtherMusclesBottomSheet = false)
                }
                CreateExerciseAction.ExerciseTypeClicked -> {
                    // Select exercise type
                }
            }
        },
        modifier = modifier
    )
}
