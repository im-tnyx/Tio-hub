package com.tnyx.features.workout.presentation.library

import androidx.compose.runtime.Immutable
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType

@Immutable
data class ExerciseLibraryUiItem(
    val id: String,
    val name: String,
    val bodyPart: String,
    val category: String,
    val trackingType: ExerciseTrackingType = ExerciseTrackingType.WEIGHT_REPS,
    val isFavorite: Boolean = false,
)

@Immutable
data class ExerciseLibraryUiState(
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedCategory: String = "ALL",
    val selectedBodyPart: String = "ALL",
    val exercises: List<ExerciseLibraryUiItem> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface ExerciseLibraryAction {
    data class SearchQueryChanged(val query: String) : ExerciseLibraryAction
    data object SearchIconClicked : ExerciseLibraryAction
    data object CreateExerciseClicked : ExerciseLibraryAction
    data class CategorySelected(val category: String) : ExerciseLibraryAction
    data class BodyPartSelected(val bodyPart: String) : ExerciseLibraryAction
    data class FavoriteToggled(val exerciseId: String) : ExerciseLibraryAction
    data class ExerciseClicked(val exerciseId: String) : ExerciseLibraryAction
    data object BackClicked : ExerciseLibraryAction
}
