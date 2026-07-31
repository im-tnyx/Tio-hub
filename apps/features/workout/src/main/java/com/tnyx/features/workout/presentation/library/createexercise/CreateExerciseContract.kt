package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.runtime.Immutable

@Immutable
data class CreateExerciseUiState(
    val exerciseName: String = "",
    val equipment: String = "Select",
    val primaryMuscleGroup: String = "Select",
    val otherMuscles: String = "Select (optional)",
    val exerciseType: String = "Select",
    val isSaving: Boolean = false,
)

sealed interface CreateExerciseAction {
    data class NameChanged(val name: String) : CreateExerciseAction
    data object AddAssetClicked : CreateExerciseAction
    data object EquipmentClicked : CreateExerciseAction
    data object PrimaryMuscleClicked : CreateExerciseAction
    data object OtherMusclesClicked : CreateExerciseAction
    data object ExerciseTypeClicked : CreateExerciseAction
    data object SaveClicked : CreateExerciseAction
    data object BackClicked : CreateExerciseAction
}
