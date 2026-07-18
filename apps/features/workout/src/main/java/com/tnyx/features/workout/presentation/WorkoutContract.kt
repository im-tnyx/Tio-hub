package com.tnyx.features.workout.presentation

import androidx.compose.runtime.Immutable
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.SetType

enum class WorkoutMetricField {
    WEIGHT,
    REPS,
    DURATION,
    DISTANCE,
    STEPS,
    RPE,
}

@Immutable
data class WorkoutMetricUi(
    val field: WorkoutMetricField,
    val label: String,
    val value: String,
    val previousValue: String? = null,
)

@Immutable
data class WorkoutSetUi(
    val id: String?,
    val setNumber: Int,
    val type: SetType,
    val metrics: List<WorkoutMetricUi>,
    val rpeValue: String = "",
    val previousRpe: String? = null,
    val previousSummary: String? = null,
    val isCompleted: Boolean,
    val completedSummary: String? = null,
)

@Immutable
data class WorkoutRpePickerUi(
    val exerciseEntryId: String,
    val setId: String?,
    val setNumber: Int,
    val selectedRpe: Int?,
)

@Immutable
data class WorkoutExerciseUi(
    val id: String,
    val name: String,
    val trackingType: ExerciseTrackingType,
    val restSeconds: Int,
    val sets: List<WorkoutSetUi>,
    val isExpanded: Boolean = true,
)

@Immutable
data class WorkoutHistoryItemUi(
    val id: String,
    val title: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val completedSets: Int,
    val totalReps: Int,
)

@Immutable
data class WorkoutUiState(
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val activeSessionId: String? = null,
    val activeStartedAtMs: Long? = null,
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    val history: List<WorkoutHistoryItemUi> = emptyList(),
    val rpePicker: WorkoutRpePickerUi? = null,
    val errorMessage: String? = null,
) {
    val hasActiveSession: Boolean
        get() = activeSessionId != null

    val hasCompletedSet: Boolean
        get() = exercises.any { exercise -> exercise.sets.any(WorkoutSetUi::isCompleted) }

    companion object {
        const val DEFAULT_REPS = "10"
    }
}

sealed interface WorkoutAction {
    data object StartBlankWorkoutClicked : WorkoutAction
    data object AddStarterExerciseClicked : WorkoutAction
    data class AddSetClicked(val exerciseEntryId: String) : WorkoutAction
    data class ExerciseExpandedChanged(
        val exerciseEntryId: String,
        val isExpanded: Boolean,
    ) : WorkoutAction

    data class MetricChanged(
        val exerciseEntryId: String,
        val setId: String?,
        val field: WorkoutMetricField,
        val value: String,
    ) : WorkoutAction

    data class CompleteSetClicked(
        val exerciseEntryId: String,
        val setId: String?,
    ) : WorkoutAction

    data class PreviousSetClicked(
        val exerciseEntryId: String,
        val setId: String?,
    ) : WorkoutAction

    data class RpeClicked(
        val exerciseEntryId: String,
        val setId: String?,
    ) : WorkoutAction

    data class RpeSelected(val value: Int) : WorkoutAction
    data object RpeDismissed : WorkoutAction

    data object FinishWorkoutClicked : WorkoutAction
    data object HistoryClicked : WorkoutAction
    data object BackClicked : WorkoutAction
}
