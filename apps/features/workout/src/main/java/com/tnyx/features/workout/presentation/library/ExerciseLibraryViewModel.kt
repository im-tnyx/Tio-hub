package com.tnyx.features.workout.presentation.library

import androidx.lifecycle.ViewModel
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor() : ViewModel() {

    private val defaultExercises = listOf(
        ExerciseLibraryUiItem(id = "lib-1", name = "Bench Press (Barbell)", bodyPart = "CHEST", category = "BARBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS, isFavorite = true),
        ExerciseLibraryUiItem(id = "lib-2", name = "Incline Dumbbell Bench Press", bodyPart = "CHEST", category = "DUMBBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-3", name = "Cable Crossover", bodyPart = "CHEST", category = "CABLE", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-4", name = "Barbell Squat", bodyPart = "LEGS", category = "BARBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS, isFavorite = true),
        ExerciseLibraryUiItem(id = "lib-5", name = "Leg Press", bodyPart = "LEGS", category = "MACHINE", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-6", name = "Romanian Deadlift", bodyPart = "LEGS", category = "BARBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-7", name = "Conventional Deadlift", bodyPart = "BACK", category = "BARBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS, isFavorite = true),
        ExerciseLibraryUiItem(id = "lib-8", name = "Lat Pulldown (Cable)", bodyPart = "BACK", category = "CABLE", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-9", name = "Pull Up", bodyPart = "BACK", category = "BODYWEIGHT", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-10", name = "Seated Overhead Dumbbell Press", bodyPart = "SHOULDERS", category = "DUMBBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-11", name = "Lateral Raise (Dumbbell)", bodyPart = "SHOULDERS", category = "DUMBBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-12", name = "Biceps Curl (Dumbbell)", bodyPart = "ARMS", category = "DUMBBELL", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-13", name = "Triceps Pushdown (Cable)", bodyPart = "ARMS", category = "CABLE", trackingType = ExerciseTrackingType.WEIGHT_REPS),
        ExerciseLibraryUiItem(id = "lib-14", name = "Plank", bodyPart = "CORE", category = "BODYWEIGHT", trackingType = ExerciseTrackingType.DURATION),
    )

    private val _uiState = MutableStateFlow(
        ExerciseLibraryUiState(
            exercises = defaultExercises,
        )
    )
    val uiState = _uiState.asStateFlow()

    fun handleAction(action: ExerciseLibraryAction) {
        when (action) {
            ExerciseLibraryAction.SearchIconClicked -> {
                _uiState.update { it.copy(isSearchActive = !it.isSearchActive) }
            }
            is ExerciseLibraryAction.SearchQueryChanged -> {
                val newQuery = action.query
                _uiState.update { state ->
                    state.copy(
                        searchQuery = newQuery,
                        exercises = filterExercises(newQuery, state.selectedBodyPart)
                    )
                }
            }
            is ExerciseLibraryAction.BodyPartSelected -> {
                val newBodyPart = action.bodyPart
                _uiState.update { state ->
                    state.copy(
                        selectedBodyPart = newBodyPart,
                        exercises = filterExercises(state.searchQuery, newBodyPart)
                    )
                }
            }
            is ExerciseLibraryAction.FavoriteToggled -> {
                val targetId = action.exerciseId
                _uiState.update { state ->
                    val updatedList = defaultExercises.map { item ->
                        if (item.id == targetId) item.copy(isFavorite = !item.isFavorite) else item
                    }
                    state.copy(exercises = filterList(updatedList, state.searchQuery, state.selectedBodyPart))
                }
            }
            is ExerciseLibraryAction.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = action.category) }
            }
            ExerciseLibraryAction.CreateExerciseClicked -> {
                // Open Create Exercise flow
            }
            is ExerciseLibraryAction.ExerciseClicked -> {
                // Open Exercise details flow
            }
            ExerciseLibraryAction.BackClicked -> {
                // Navigate back
            }
        }
    }

    private fun filterExercises(query: String, bodyPart: String): List<ExerciseLibraryUiItem> {
        return filterList(defaultExercises, query, bodyPart)
    }

    private fun filterList(
        source: List<ExerciseLibraryUiItem>,
        query: String,
        bodyPart: String
    ): List<ExerciseLibraryUiItem> {
        return source.filter { item ->
            val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true)
            val matchesBodyPart = bodyPart == "ALL" || item.bodyPart.equals(bodyPart, ignoreCase = true)
            matchesQuery && matchesBodyPart
        }
    }
}
