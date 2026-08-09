package com.tnyx.features.workout.presentation.library.createexercise

import androidx.compose.runtime.Immutable

@Immutable
data class CreateExerciseUiState(
    val exerciseId: String? = null,
    val isEditMode: Boolean = false,
    val exerciseName: String = "",
    val instructions: String = "",
    val equipment: String = "Select (optional)",
    val bodyPart: String = "Select (optional)",
    val primaryMuscleGroup: String = "Select",
    val otherMuscles: String = "Select (optional)",
    val exerciseType: String = "Select",
    val assetUri: String? = null,
    val showImageSourceBottomSheet: Boolean = false,
    val showEquipmentBottomSheet: Boolean = false,
    val showBodyPartBottomSheet: Boolean = false,
    val showPrimaryMuscleBottomSheet: Boolean = false,
    val showOtherMusclesBottomSheet: Boolean = false,
    val showExerciseTypeBottomSheet: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface CreateExerciseAction {
    data class NameChanged(val name: String) : CreateExerciseAction
    data class InstructionsChanged(val instructions: String) : CreateExerciseAction
    data class AssetSelected(val uri: String?) : CreateExerciseAction
    data object AddAssetClicked : CreateExerciseAction
    data object RemoveAssetClicked : CreateExerciseAction
    data object CameraClicked : CreateExerciseAction
    data object GalleryClicked : CreateExerciseAction
    data object ImageSourceBottomSheetDismissed : CreateExerciseAction
    data object EquipmentClicked : CreateExerciseAction
    data object BodyPartClicked : CreateExerciseAction
    data object PrimaryMuscleClicked : CreateExerciseAction
    data object OtherMusclesClicked : CreateExerciseAction
    data class EquipmentSelected(val equipment: String) : CreateExerciseAction
    data class BodyPartSelected(val bodyPart: String) : CreateExerciseAction
    data class PrimaryMuscleSelected(val muscle: String) : CreateExerciseAction
    data class OtherMusclesSelected(val muscle: String) : CreateExerciseAction
    data class ExerciseTypeSelected(val exerciseType: String) : CreateExerciseAction
    data object EquipmentBottomSheetDismissed : CreateExerciseAction
    data object BodyPartBottomSheetDismissed : CreateExerciseAction
    data object PrimaryMuscleBottomSheetDismissed : CreateExerciseAction
    data object OtherMusclesBottomSheetDismissed : CreateExerciseAction
    data object ExerciseTypeBottomSheetDismissed : CreateExerciseAction
    data object ExerciseTypeClicked : CreateExerciseAction
    data object SaveClicked : CreateExerciseAction
    data object BackClicked : CreateExerciseAction
}
