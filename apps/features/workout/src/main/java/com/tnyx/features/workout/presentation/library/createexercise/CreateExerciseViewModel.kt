package com.tnyx.features.workout.presentation.library.createexercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.navigation.WorkoutDestination
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface CreateExerciseEvent {
    data object SaveSuccess : CreateExerciseEvent
    data class SaveError(val message: String) : CreateExerciseEvent
}

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: ExerciseCatalogRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<WorkoutDestination.CreateExercise>()

    private val _uiState = MutableStateFlow(CreateExerciseUiState())
    val uiState: StateFlow<CreateExerciseUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CreateExerciseEvent>()
    val eventFlow: SharedFlow<CreateExerciseEvent> = _eventFlow.asSharedFlow()

    init {
        val exerciseId = route.exerciseId
        if (!exerciseId.isNullOrBlank()) {
            viewModelScope.launch {
                val exercise = catalogRepository.getExerciseById(exerciseId)
                if (exercise?.isCustom == true) {
                    _uiState.value = exercise.toEditUiState()
                }
            }
        }
    }

    fun onAction(action: CreateExerciseAction) {
        when (action) {
            is CreateExerciseAction.NameChanged -> {
                _uiState.update { it.copy(exerciseName = action.name) }
            }
            is CreateExerciseAction.InstructionsChanged -> {
                _uiState.update { it.copy(instructions = action.instructions) }
            }
            is CreateExerciseAction.AssetSelected -> {
                _uiState.update { it.copy(assetUri = action.uri) }
            }
            CreateExerciseAction.AddAssetClicked -> {
                _uiState.update { it.copy(showImageSourceBottomSheet = true) }
            }
            CreateExerciseAction.RemoveAssetClicked -> {
                _uiState.update { it.copy(assetUri = null, showImageSourceBottomSheet = false) }
            }
            CreateExerciseAction.ImageSourceBottomSheetDismissed -> {
                _uiState.update { it.copy(showImageSourceBottomSheet = false) }
            }
            CreateExerciseAction.CameraClicked,
            CreateExerciseAction.GalleryClicked -> {
                _uiState.update { it.copy(showImageSourceBottomSheet = false) }
            }
            CreateExerciseAction.EquipmentClicked -> {
                _uiState.update { it.copy(showEquipmentBottomSheet = true) }
            }
            is CreateExerciseAction.EquipmentSelected -> {
                _uiState.update {
                    it.copy(
                        equipment = action.equipment,
                        showEquipmentBottomSheet = false
                    )
                }
            }
            CreateExerciseAction.EquipmentBottomSheetDismissed -> {
                _uiState.update { it.copy(showEquipmentBottomSheet = false) }
            }
            CreateExerciseAction.BodyPartClicked -> {
                _uiState.update { it.copy(showBodyPartBottomSheet = true) }
            }
            is CreateExerciseAction.BodyPartSelected -> {
                _uiState.update {
                    it.copy(
                        bodyPart = action.bodyPart,
                        primaryMuscleGroup = "Select",
                    )
                }
            }
            CreateExerciseAction.BodyPartBottomSheetDismissed -> {
                _uiState.update { it.copy(showBodyPartBottomSheet = false) }
            }
            CreateExerciseAction.PrimaryMuscleClicked -> {
                _uiState.update { it.copy(showPrimaryMuscleBottomSheet = true) }
            }
            is CreateExerciseAction.PrimaryMuscleSelected -> {
                _uiState.update { it.copy(primaryMuscleGroup = action.muscle) }
            }
            CreateExerciseAction.PrimaryMuscleBottomSheetDismissed -> {
                _uiState.update { it.copy(showPrimaryMuscleBottomSheet = false) }
            }
            CreateExerciseAction.OtherMusclesClicked -> {
                _uiState.update { it.copy(showOtherMusclesBottomSheet = true) }
            }
            is CreateExerciseAction.OtherMusclesSelected -> {
                _uiState.update { it.copy(otherMuscles = action.muscle) }
            }
            CreateExerciseAction.OtherMusclesBottomSheetDismissed -> {
                _uiState.update { it.copy(showOtherMusclesBottomSheet = false) }
            }
            CreateExerciseAction.ExerciseTypeClicked -> {
                _uiState.update { it.copy(showExerciseTypeBottomSheet = true) }
            }
            is CreateExerciseAction.ExerciseTypeSelected -> {
                _uiState.update {
                    it.copy(
                        exerciseType = action.exerciseType,
                        showExerciseTypeBottomSheet = false
                    )
                }
            }
            CreateExerciseAction.ExerciseTypeBottomSheetDismissed -> {
                _uiState.update { it.copy(showExerciseTypeBottomSheet = false) }
            }
            CreateExerciseAction.SaveClicked -> {
                saveExercise()
            }
            CreateExerciseAction.BackClicked -> {
                // Handled at navigation level
            }
        }
    }

    private fun saveExercise() {
        val currentState = _uiState.value
        if (currentState.exerciseName.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(CreateExerciseEvent.SaveError("Exercise name cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val exerciseDefinition = ExerciseDefinition(
                id = currentState.exerciseId ?: UUID.randomUUID().toString(),
                name = currentState.exerciseName.trim(),
                bodyPart = currentState.bodyPart
                    .trim()
                    .takeIf { it.isNotEmpty() && !it.contains("select", ignoreCase = true) && !it.contains("optional", ignoreCase = true) }
                    ?.lowercase(),
                primaryMuscleGroups = currentState.primaryMuscleGroup.split(",")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() && !it.contains("select", ignoreCase = true) },
                secondaryMuscleGroups = currentState.otherMuscles.split(",")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() && !it.contains("select", ignoreCase = true) && !it.contains("optional", ignoreCase = true) },
                equipment = currentState.equipment.split(",")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() && !it.contains("select", ignoreCase = true) && !it.contains("optional", ignoreCase = true) },
                instructions = if (currentState.instructions.isNotBlank()) listOf(currentState.instructions.trim()) else emptyList(),
                trackingType = mapToTrackingType(currentState.exerciseType),
                isCustom = true,
            )

            try {
                catalogRepository.saveCustomExercise(exerciseDefinition)
                _eventFlow.emit(CreateExerciseEvent.SaveSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(CreateExerciseEvent.SaveError(e.message ?: "Failed to save exercise"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun mapToTrackingType(typeString: String): ExerciseTrackingType {
        return when {
            typeString.contains("Distance", ignoreCase = true) -> ExerciseTrackingType.DISTANCE_DURATION
            typeString.contains("Duration", ignoreCase = true) -> ExerciseTrackingType.DURATION
            typeString.contains("Steps", ignoreCase = true) -> ExerciseTrackingType.STEPS_DURATION
            typeString.contains("Assisted", ignoreCase = true) -> ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS
            typeString.contains("Bodyweight", ignoreCase = true) -> ExerciseTrackingType.BODYWEIGHT_REPS
            else -> ExerciseTrackingType.WEIGHT_REPS
        }
    }
}

private fun ExerciseDefinition.toEditUiState(): CreateExerciseUiState {
    return CreateExerciseUiState(
        exerciseId = id,
        isEditMode = true,
        exerciseName = name,
        instructions = instructions.joinToString(separator = "\n"),
        equipment = equipment.joinToStringOrDefault("Select (optional)"),
        bodyPart = bodyPart?.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        } ?: "Select (optional)",
        primaryMuscleGroup = primaryMuscleGroups.joinToStringOrDefault("Select"),
        otherMuscles = secondaryMuscleGroups.joinToStringOrDefault("Select (optional)"),
        exerciseType = trackingType.toUiLabel(),
        assetUri = mediaAssets.firstOrNull()?.imageRef ?: mediaAssets.firstOrNull()?.thumbnailRef ?: mediaAssets.firstOrNull()?.videoRef,
    )
}

private fun List<String>.joinToStringOrDefault(defaultValue: String): String {
    return if (isEmpty()) {
        defaultValue
    } else {
        joinToString(separator = ", ") { value ->
            value.replace("_", " ").split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }
                }
        }
    }
}

private fun ExerciseTrackingType.toUiLabel(): String {
    return when (this) {
        ExerciseTrackingType.WEIGHT_REPS -> "Weight & Reps"
        ExerciseTrackingType.BODYWEIGHT_REPS -> "Bodyweight Reps"
        ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS -> "Assisted Bodyweight Reps"
        ExerciseTrackingType.DURATION -> "Duration"
        ExerciseTrackingType.DISTANCE_DURATION -> "Distance & Duration"
        ExerciseTrackingType.STEPS_DURATION -> "Steps & Duration"
    }
}
