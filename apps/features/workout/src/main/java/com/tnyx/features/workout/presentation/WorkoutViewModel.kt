package com.tnyx.features.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.features.workout.domain.WorkoutCommandResult
import com.tnyx.features.workout.domain.WorkoutDashboard
import com.tnyx.features.workout.domain.WorkoutInputError
import com.tnyx.features.workout.domain.WorkoutSessionCoordinator
import com.tnyx.shared.workout.domain.model.ExerciseTrackingType
import com.tnyx.shared.workout.domain.model.SetType
import com.tnyx.shared.workout.domain.model.WorkoutSet
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val coordinator: WorkoutSessionCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()
    private val commandInFlight = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            coordinator.observeDashboard().collect { dashboard ->
                _uiState.update { current -> current.withDashboard(dashboard) }
            }
        }
    }

    fun handleAction(action: WorkoutAction) {
        when (action) {
            WorkoutAction.StartBlankWorkoutClicked -> runCommand(coordinator::startBlankWorkout)
            WorkoutAction.AddStarterExerciseClicked -> runCommand(coordinator::addStarterExercise)
            is WorkoutAction.AddSetClicked -> addSet(action)
            is WorkoutAction.ExerciseExpandedChanged -> updateExpandedState(action)
            is WorkoutAction.MetricChanged -> updateMetric(action)
            is WorkoutAction.CompleteSetClicked -> completeSet(action)
            is WorkoutAction.PreviousSetClicked -> copyPreviousSet(action)
            is WorkoutAction.RpeClicked -> openRpePicker(action)
            is WorkoutAction.RpeSelected -> selectRpe(action.value)
            WorkoutAction.RpeDismissed -> _uiState.update { it.copy(rpePicker = null) }
            WorkoutAction.FinishWorkoutClicked -> runCommand(coordinator::finishWorkout)
            WorkoutAction.HistoryClicked,
            WorkoutAction.BackClicked,
            -> Unit
        }
    }

    private fun addSet(action: WorkoutAction.AddSetClicked) {
        val exercise = _uiState.value.exercises.firstOrNull { it.id == action.exerciseEntryId }
        if (exercise == null || exercise.sets.any { it.id == null }) {
            _uiState.update { it.copy(errorMessage = "Complete the current set before adding another.") }
            return
        }
        runCommand { coordinator.addSet(action.exerciseEntryId) }
    }

    private fun updateExpandedState(action: WorkoutAction.ExerciseExpandedChanged) {
        _uiState.update { state ->
            val exerciseIndex = state.exercises.indexOfFirst { it.id == action.exerciseEntryId }
            if (exerciseIndex < 0) return@update state
            val updatedExercise = state.exercises[exerciseIndex].copy(isExpanded = action.isExpanded)
            state.copy(
                exercises = state.exercises.toMutableList().apply {
                    set(exerciseIndex, updatedExercise)
                },
            )
        }
    }

    private fun updateMetric(action: WorkoutAction.MetricChanged) {
        if (_uiState.value.isMutating) return
        val sanitized = when (action.field) {
            WorkoutMetricField.WEIGHT,
            WorkoutMetricField.DISTANCE,
            -> action.value.sanitizeDecimal(MAX_DECIMAL_DIGITS)

            WorkoutMetricField.REPS -> action.value.filter(Char::isDigit).take(MAX_REPS_DIGITS)
            WorkoutMetricField.DURATION,
            WorkoutMetricField.STEPS,
            -> action.value.filter(Char::isDigit).take(MAX_INTEGER_DIGITS)

            WorkoutMetricField.RPE -> action.value.filter(Char::isDigit).take(MAX_RPE_DIGITS)
        }
        _uiState.update { state ->
            val exerciseIndex = state.exercises.indexOfFirst { it.id == action.exerciseEntryId }
            if (exerciseIndex < 0) return@update state
            val exercise = state.exercises[exerciseIndex]
            val setIndex = exercise.sets.indexOfFirst { it.id == action.setId }
            if (setIndex < 0 || exercise.sets[setIndex].isCompleted) return@update state
            val currentSet = exercise.sets[setIndex]
            val updatedSet = currentSet.copy(
                metrics = currentSet.metrics.map { metric ->
                    if (metric.field == action.field) metric.copy(value = sanitized) else metric
                },
            )
            state.copy(
                exercises = state.exercises.toMutableList().apply {
                    set(
                        exerciseIndex,
                        exercise.copy(
                            sets = exercise.sets.toMutableList().apply { set(setIndex, updatedSet) },
                        ),
                    )
                },
                errorMessage = null,
            )
        }
    }

    private fun completeSet(action: WorkoutAction.CompleteSetClicked) {
        val exercise = _uiState.value.exercises.firstOrNull { it.id == action.exerciseEntryId }
        val set = exercise?.sets?.firstOrNull { it.id == action.setId }
        if (set == null) {
            _uiState.update { it.copy(errorMessage = "Workout set is no longer available.") }
            return
        }
        val reps = set.metrics
            .firstOrNull { it.field == WorkoutMetricField.REPS }
            ?.value
            ?.toIntOrNull()
        if (reps == null) {
            _uiState.update { it.copy(errorMessage = "Enter reps from 1 to 999.") }
            return
        }
        val rpe = set.rpeValue
            .takeIf(String::isNotBlank)
            ?.toIntOrNull()
        runCommand {
            coordinator.completeSet(
                exerciseEntryId = action.exerciseEntryId,
                setId = action.setId,
                reps = reps,
                rpe = rpe,
            )
        }
    }

    private fun copyPreviousSet(action: WorkoutAction.PreviousSetClicked) {
        _uiState.update { state ->
            state.updateSet(action.exerciseEntryId, action.setId) { set ->
                if (set.isCompleted || set.previousSummary == null) return@updateSet set
                set.copy(
                    metrics = set.metrics.map { metric ->
                        metric.previousValue?.let { metric.copy(value = it) } ?: metric
                    },
                    rpeValue = set.previousRpe ?: set.rpeValue,
                )
            }.copy(errorMessage = null)
        }
    }

    private fun openRpePicker(action: WorkoutAction.RpeClicked) {
        val exercise = _uiState.value.exercises.firstOrNull { it.id == action.exerciseEntryId } ?: return
        val set = exercise.sets.firstOrNull { it.id == action.setId } ?: return
        if (set.isCompleted) return
        val selected = set.rpeValue.toIntOrNull()
        _uiState.update {
            it.copy(
                rpePicker = WorkoutRpePickerUi(
                    exerciseEntryId = exercise.id,
                    setId = set.id,
                    setNumber = set.setNumber,
                    selectedRpe = selected,
                ),
            )
        }
    }

    private fun selectRpe(value: Int) {
        val picker = _uiState.value.rpePicker ?: return
        if (value !in MIN_RPE..MAX_RPE) return
        _uiState.update { state ->
            state.updateSet(picker.exerciseEntryId, picker.setId) { set ->
                if (set.isCompleted) return@updateSet set
                set.copy(rpeValue = value.toString())
            }.copy(rpePicker = null, errorMessage = null)
        }
    }

    private fun runCommand(command: suspend () -> WorkoutCommandResult) {
        if (!commandInFlight.compareAndSet(false, true)) return
        _uiState.update { it.copy(isMutating = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = command()
                _uiState.update { state -> state.copy(errorMessage = result.userMessage()) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Workout could not be saved. Try again.")
                }
            } finally {
                commandInFlight.set(false)
                _uiState.update { it.copy(isMutating = false) }
            }
        }
    }

    private fun WorkoutUiState.withDashboard(dashboard: WorkoutDashboard): WorkoutUiState {
        val activeSession = dashboard.engineState.session?.takeIf { it.isActive }
        val mappedExercises = activeSession?.exercises.orEmpty().map { sessionExercise ->
            val currentExercise = exercises.firstOrNull { it.id == sessionExercise.id }
            val previousExercise = dashboard.history
                .asSequence()
                .filter { it.status == WorkoutSessionStatus.COMPLETED }
                .sortedByDescending { it.endedAtMs ?: it.startedAtMs }
                .mapNotNull { historySession ->
                    historySession.exercises.firstOrNull { it.exerciseId == sessionExercise.exerciseId }
                }
                .firstOrNull()
            val persistedSets: List<WorkoutSet?> = if (sessionExercise.sets.isEmpty()) {
                listOf(null)
            } else {
                sessionExercise.sets
            }
            WorkoutExerciseUi(
                id = sessionExercise.id,
                name = sessionExercise.exerciseNameSnapshot,
                trackingType = sessionExercise.trackingTypeSnapshot,
                restSeconds = sessionExercise.restSeconds,
                sets = persistedSets.mapIndexed { index, persistedSet ->
                    val setNumber = persistedSet?.setNumber ?: index + 1
                    val existingSet = currentExercise?.sets?.firstOrNull { candidate ->
                        (persistedSet?.id != null && candidate.id == persistedSet.id) ||
                            candidate.setNumber == setNumber
                    }
                    persistedSet.toUi(
                        setNumber = setNumber,
                        trackingType = sessionExercise.trackingTypeSnapshot,
                        existing = existingSet,
                        previous = previousExercise?.sets?.firstOrNull { it.setNumber == setNumber },
                    )
                },
                isExpanded = currentExercise?.isExpanded ?: true,
            )
        }
        return copy(
            isLoading = false,
            activeSessionId = activeSession?.id,
            activeStartedAtMs = activeSession?.startedAtMs,
            exercises = mappedExercises,
            history = dashboard.history.map { session ->
                WorkoutHistoryItemUi(
                    id = session.id,
                    title = session.routineName.ifBlank { "Blank workout" },
                    startedAtMs = session.startedAtMs,
                    endedAtMs = requireNotNull(session.endedAtMs),
                    completedSets = session.completedSets,
                    totalReps = session.sets.filter { it.isCompleted }.sumOf { it.reps ?: 0 },
                )
            },
        )
    }

    private fun WorkoutSet?.toUi(
        setNumber: Int,
        trackingType: ExerciseTrackingType,
        existing: WorkoutSetUi?,
        previous: WorkoutSet?,
    ): WorkoutSetUi = WorkoutSetUi(
        id = this?.id,
        setNumber = setNumber,
        type = this?.type ?: SetType.NORMAL,
        metrics = trackingType.metricDefinitions().map { definition ->
            val persistedValue = valueFor(definition.field)
            val existingValue = existing?.metrics
                ?.firstOrNull { it.field == definition.field }
                ?.value
            WorkoutMetricUi(
                field = definition.field,
                label = definition.label,
                previousValue = previous.valueFor(definition.field),
                value = when {
                    this?.isCompleted == true -> persistedValue.orEmpty()
                    existingValue != null -> existingValue
                    persistedValue != null -> persistedValue
                    definition.field == WorkoutMetricField.REPS -> WorkoutUiState.DEFAULT_REPS
                    else -> ""
                },
            )
        },
        rpeValue = when {
            this?.isCompleted == true -> this.rpe?.toString().orEmpty()
            existing != null -> existing.rpeValue
            else -> this?.rpe?.toString().orEmpty()
        },
        previousRpe = previous?.rpe?.toString(),
        previousSummary = previous
            ?.takeIf { it.isCompleted }
            ?.previousSummary(trackingType),
        isCompleted = this?.isCompleted == true,
        completedSummary = this?.takeIf { it.isCompleted }?.summary(trackingType),
    )

    private fun WorkoutCommandResult.userMessage(): String? = when (this) {
        is WorkoutCommandResult.Success -> null
        is WorkoutCommandResult.InvalidInput -> when (error) {
            WorkoutInputError.REPS_OUT_OF_RANGE -> "Enter reps from 1 to 999."
            WorkoutInputError.RPE_OUT_OF_RANGE -> "Select an RPE from 5 to 10."
            WorkoutInputError.COMPLETE_A_SET_FIRST -> "Complete one set before finishing."
        }

        is WorkoutCommandResult.Rejected -> "Workout changed before this action was saved. Try again."
    }

    private companion object {
        const val MAX_REPS_DIGITS = 3
        const val MAX_INTEGER_DIGITS = 6
        const val MAX_DECIMAL_DIGITS = 8
        const val MAX_RPE_DIGITS = 2
        const val MIN_RPE = 5
        const val MAX_RPE = 10
    }
}

private data class MetricDefinition(
    val field: WorkoutMetricField,
    val label: String,
)

private fun ExerciseTrackingType.metricDefinitions(): List<MetricDefinition> = when (this) {
    ExerciseTrackingType.WEIGHT_REPS -> listOf(
        MetricDefinition(WorkoutMetricField.WEIGHT, "Weight (kg)"),
        MetricDefinition(WorkoutMetricField.REPS, "Reps"),
    )

    ExerciseTrackingType.BODYWEIGHT_REPS -> listOf(
        MetricDefinition(WorkoutMetricField.REPS, "Reps"),
    )

    ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS -> listOf(
        MetricDefinition(WorkoutMetricField.WEIGHT, "Assistance (kg)"),
        MetricDefinition(WorkoutMetricField.REPS, "Reps"),
    )

    ExerciseTrackingType.DURATION -> listOf(
        MetricDefinition(WorkoutMetricField.DURATION, "Duration (sec)"),
    )

    ExerciseTrackingType.DISTANCE_DURATION -> listOf(
        MetricDefinition(WorkoutMetricField.DISTANCE, "Distance (m)"),
        MetricDefinition(WorkoutMetricField.DURATION, "Duration (sec)"),
    )

    ExerciseTrackingType.STEPS_DURATION -> listOf(
        MetricDefinition(WorkoutMetricField.STEPS, "Steps"),
        MetricDefinition(WorkoutMetricField.DURATION, "Duration (sec)"),
    )
}

private fun WorkoutSet?.valueFor(field: WorkoutMetricField): String? = when (field) {
    WorkoutMetricField.WEIGHT -> this?.weightKg?.toInputText()
    WorkoutMetricField.REPS -> this?.reps?.toString()
    WorkoutMetricField.DURATION -> this?.durationSeconds?.toString()
    WorkoutMetricField.DISTANCE -> this?.distanceMeters?.toInputText()
    WorkoutMetricField.STEPS -> this?.steps?.toString()
    WorkoutMetricField.RPE -> this?.rpe?.toString()
}

private fun WorkoutSet.previousSummary(trackingType: ExerciseTrackingType): String {
    val effort = rpe?.let { " · RPE $it" }.orEmpty()
    return when (trackingType) {
        ExerciseTrackingType.WEIGHT_REPS ->
            "${weightKg?.toInputText() ?: "—"} × ${reps ?: "—"}$effort"
        ExerciseTrackingType.BODYWEIGHT_REPS -> "× ${reps ?: "—"}$effort"
        ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS ->
            "${weightKg?.toInputText() ?: "—"} assist × ${reps ?: "—"}$effort"
        ExerciseTrackingType.DURATION -> durationSeconds.toClockText()
        ExerciseTrackingType.DISTANCE_DURATION ->
            "${distanceMeters?.toInputText() ?: "—"} m · ${durationSeconds.toClockText()}"
        ExerciseTrackingType.STEPS_DURATION ->
            "${steps ?: "—"} steps · ${durationSeconds.toClockText()}"
    }
}

private fun WorkoutSet.summary(trackingType: ExerciseTrackingType): String = when (trackingType) {
    ExerciseTrackingType.WEIGHT_REPS -> weightKg?.let { "${it.toInputText()} kg × ${reps ?: 0} reps" }
        ?: "${reps ?: 0} reps"
    ExerciseTrackingType.BODYWEIGHT_REPS -> "${reps ?: 0} reps"
    ExerciseTrackingType.ASSISTED_BODYWEIGHT_REPS ->
        weightKg?.let { "${it.toInputText()} kg assist × ${reps ?: 0} reps" }
            ?: "${reps ?: 0} reps"
    ExerciseTrackingType.DURATION -> "${durationSeconds ?: 0} sec"
    ExerciseTrackingType.DISTANCE_DURATION ->
        "${distanceMeters?.toInputText() ?: 0} m · ${durationSeconds ?: 0} sec"
    ExerciseTrackingType.STEPS_DURATION -> "${steps ?: 0} steps · ${durationSeconds ?: 0} sec"
}

private fun String.sanitizeDecimal(maxLength: Int): String = buildString {
    var hasDecimal = false
    for (character in this@sanitizeDecimal) {
        when {
            character.isDigit() -> append(character)
            character == '.' && !hasDecimal -> {
                append(character)
                hasDecimal = true
            }
        }
        if (length >= maxLength) break
    }
}

private fun Double.toInputText(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()

private fun Int?.toClockText(): String {
    val totalSeconds = this ?: return "—"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private inline fun WorkoutUiState.updateSet(
    exerciseEntryId: String,
    setId: String?,
    transform: (WorkoutSetUi) -> WorkoutSetUi,
): WorkoutUiState {
    val exerciseIndex = exercises.indexOfFirst { it.id == exerciseEntryId }
    if (exerciseIndex < 0) return this
    val exercise = exercises[exerciseIndex]
    val setIndex = exercise.sets.indexOfFirst { it.id == setId }
    if (setIndex < 0) return this
    val updatedSets = exercise.sets.toMutableList().apply {
        set(setIndex, transform(exercise.sets[setIndex]))
    }
    return copy(
        exercises = exercises.toMutableList().apply {
            set(exerciseIndex, exercise.copy(sets = updatedSets))
        },
    )
}
