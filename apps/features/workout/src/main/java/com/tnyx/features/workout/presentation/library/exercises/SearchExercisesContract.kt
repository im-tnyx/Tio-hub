package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.runtime.Immutable
import com.tnyx.shared.workout.domain.model.ExerciseDefinition

enum class ExerciseViewType { LIST, GRID }

@Immutable
data class SearchExercisesUiState(
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedFilter: String = "ALL",
    val availableFilters: List<String> = listOf("ALL", "CHEST", "TRICEPS", "BICEPS", "BACK", "LEGS", "SHOULDERS", "ABS", "LOWER_BACK"),
    val exercises: List<ExerciseDefinition> = emptyList(),
    val isLoading: Boolean = false,
    val viewType: ExerciseViewType = ExerciseViewType.LIST,
    val selectedExerciseForActions: ExerciseDefinition? = null,
)

sealed interface SearchExercisesAction {
    data class QueryChanged(val query: String) : SearchExercisesAction
    data class FilterSelected(val filter: String) : SearchExercisesAction
    data object SearchIconClicked : SearchExercisesAction
    data object SearchModeDismissed : SearchExercisesAction
    data object FilterIconClicked : SearchExercisesAction
    data object CreateIconClicked : SearchExercisesAction
    data object BackClicked : SearchExercisesAction
    data object ToggleViewType : SearchExercisesAction
    data class ExerciseInfoClicked(val exerciseId: String) : SearchExercisesAction
    data class ExerciseSelected(val exerciseId: String) : SearchExercisesAction
    data class ExerciseLongClicked(val exerciseId: String) : SearchExercisesAction
    data object ExerciseActionsDismissed : SearchExercisesAction
}
