package com.tnyx.shared.workout.domain.logic

import com.tnyx.shared.workout.domain.model.ExerciseAdded
import com.tnyx.shared.workout.domain.model.ExerciseRemoved
import com.tnyx.shared.workout.domain.model.ExerciseReordered
import com.tnyx.shared.workout.domain.model.ExerciseSkipChanged
import com.tnyx.shared.workout.domain.model.RestTimerAdjusted
import com.tnyx.shared.workout.domain.model.RestTimerStarted
import com.tnyx.shared.workout.domain.model.RestTimerState
import com.tnyx.shared.workout.domain.model.RestTimerStatus
import com.tnyx.shared.workout.domain.model.RestTimerStopped
import com.tnyx.shared.workout.domain.model.SessionDiscarded
import com.tnyx.shared.workout.domain.model.SessionFinished
import com.tnyx.shared.workout.domain.model.SessionNotesUpdated
import com.tnyx.shared.workout.domain.model.SessionStarted
import com.tnyx.shared.workout.domain.model.SetRemoved
import com.tnyx.shared.workout.domain.model.SetUpserted
import com.tnyx.shared.workout.domain.model.WORKOUT_CONTRACT_VERSION
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutExercise
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutSession
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.model.WorkoutSet

enum class WorkoutMutationRejection {
    UNSUPPORTED_VERSION,
    INVALID_MUTATION,
    SESSION_ALREADY_ACTIVE,
    NO_ACTIVE_SESSION,
    SESSION_NOT_ACTIVE,
    SESSION_ID_MISMATCH,
    INVALID_EXERCISE,
    EXERCISE_ALREADY_EXISTS,
    EXERCISE_NOT_FOUND,
    INVALID_POSITION,
    INVALID_SET,
    SET_ID_CONFLICT,
    SET_NUMBER_CONFLICT,
    SET_NOT_FOUND,
    INVALID_TIMER,
    INVALID_TIMER_STATE,
    INVALID_END_TIME
}

sealed interface WorkoutReductionResult {
    val state: WorkoutEngineState

    data class Applied(
        override val state: WorkoutEngineState,
        val mutationId: String
    ) : WorkoutReductionResult

    data class Rejected(
        override val state: WorkoutEngineState,
        val mutationId: String,
        val reason: WorkoutMutationRejection
    ) : WorkoutReductionResult
}

object WorkoutReducer {
    private const val MAX_REST_SECONDS = 86_400

    fun reduce(
        state: WorkoutEngineState,
        mutation: WorkoutMutation
    ): WorkoutReductionResult {
        validateEnvelope(state, mutation)?.let { reason ->
            return rejected(state, mutation, reason)
        }

        return when (val payload = mutation.payload) {
            is SessionStarted -> startSession(state, mutation, payload)
            is ExerciseAdded -> addExercise(state, mutation, payload)
            is ExerciseRemoved -> removeExercise(state, mutation, payload)
            is ExerciseReordered -> reorderExercise(state, mutation, payload)
            is ExerciseSkipChanged -> changeExerciseSkip(state, mutation, payload)
            is SetUpserted -> upsertSet(state, mutation, payload)
            is SetRemoved -> removeSet(state, mutation, payload)
            is SessionNotesUpdated -> updateSessionNotes(state, mutation, payload)
            is RestTimerStarted -> startRestTimer(state, mutation, payload)
            is RestTimerAdjusted -> adjustRestTimer(state, mutation, payload)
            RestTimerStopped -> stopRestTimer(state, mutation)
            is SessionFinished -> finishSession(state, mutation, payload)
            is SessionDiscarded -> discardSession(state, mutation, payload)
        }
    }

    private fun validateEnvelope(
        state: WorkoutEngineState,
        mutation: WorkoutMutation
    ): WorkoutMutationRejection? {
        if (state.schemaVersion != WORKOUT_CONTRACT_VERSION ||
            mutation.schemaVersion != WORKOUT_CONTRACT_VERSION
        ) {
            return WorkoutMutationRejection.UNSUPPORTED_VERSION
        }

        return if (
            mutation.mutationId.isBlank() ||
            mutation.sessionId.isBlank() ||
            mutation.originDeviceId.isBlank() ||
            mutation.originSequence < 0L ||
            mutation.occurredAtMs < 0L
        ) {
            WorkoutMutationRejection.INVALID_MUTATION
        } else {
            null
        }
    }

    private fun startSession(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: SessionStarted
    ): WorkoutReductionResult {
        if (state.session?.isActive == true) {
            return rejected(state, mutation, WorkoutMutationRejection.SESSION_ALREADY_ACTIVE)
        }
        if (payload.startedAtMs < 0L || payload.routineId?.isBlank() == true) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_MUTATION)
        }
        if (!payload.initialExercises.areValidExercises()) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_EXERCISE)
        }

        val session = WorkoutSession(
            id = mutation.sessionId,
            startedAtMs = payload.startedAtMs,
            routineId = payload.routineId,
            routineName = payload.routineName,
            exercises = payload.initialExercises.normalizedOrders(),
            revision = 1L
        )
        return WorkoutReductionResult.Applied(
            state = state.copy(session = session, restTimer = RestTimerState()),
            mutationId = mutation.mutationId
        )
    }

    private fun addExercise(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: ExerciseAdded
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        if (!payload.exercise.isValidExercise()) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_EXERCISE)
        }
        if (session.exercises.any { it.id == payload.exercise.id }) {
            return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_ALREADY_EXISTS)
        }
        val existingSetIds = session.exercises.flatMap { it.sets }.map { it.id }.toSet()
        if (payload.exercise.sets.any { it.id in existingSetIds }) {
            return rejected(state, mutation, WorkoutMutationRejection.SET_ID_CONFLICT)
        }

        val targetIndex = payload.atIndex ?: session.exercises.size
        if (targetIndex !in 0..session.exercises.size) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_POSITION)
        }

        val exercises = session.exercises.toMutableList().apply {
            add(targetIndex, payload.exercise)
        }.normalizedOrders()
        return applied(state, mutation, session.copy(exercises = exercises))
    }

    private fun removeExercise(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: ExerciseRemoved
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        if (session.exercises.none { it.id == payload.exerciseEntryId }) {
            return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_NOT_FOUND)
        }

        val exercises = session.exercises
            .filterNot { it.id == payload.exerciseEntryId }
            .normalizedOrders()
        val timer = if (state.restTimer.exerciseEntryId == payload.exerciseEntryId) {
            RestTimerState()
        } else {
            state.restTimer
        }
        return applied(state, mutation, session.copy(exercises = exercises), timer)
    }

    private fun reorderExercise(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: ExerciseReordered
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        val currentIndex = session.exercises.indexOfFirst { it.id == payload.exerciseEntryId }
        if (currentIndex < 0) {
            return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_NOT_FOUND)
        }
        if (payload.toIndex !in session.exercises.indices) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_POSITION)
        }

        val exercises = session.exercises.toMutableList().apply {
            add(payload.toIndex, removeAt(currentIndex))
        }.normalizedOrders()
        return applied(state, mutation, session.copy(exercises = exercises))
    }

    private fun changeExerciseSkip(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: ExerciseSkipChanged
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        if (session.exercises.none { it.id == payload.exerciseEntryId }) {
            return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_NOT_FOUND)
        }

        val exercises = session.exercises.map { exercise ->
            if (exercise.id == payload.exerciseEntryId) {
                exercise.copy(isSkipped = payload.isSkipped)
            } else {
                exercise
            }
        }
        val timer = if (payload.isSkipped && state.restTimer.exerciseEntryId == payload.exerciseEntryId) {
            RestTimerState()
        } else {
            state.restTimer
        }
        return applied(state, mutation, session.copy(exercises = exercises), timer)
    }

    private fun upsertSet(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: SetUpserted
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        val exercise = session.exercises.firstOrNull { it.id == payload.exerciseEntryId }
            ?: return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_NOT_FOUND)

        if (!payload.set.isValidSet() || payload.set.exerciseEntryId != payload.exerciseEntryId) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_SET)
        }

        val setOwner = session.exercises.firstOrNull { candidate ->
            candidate.sets.any { it.id == payload.set.id }
        }
        if (setOwner != null && setOwner.id != payload.exerciseEntryId) {
            return rejected(state, mutation, WorkoutMutationRejection.SET_ID_CONFLICT)
        }
        if (exercise.sets.any { it.id != payload.set.id && it.setNumber == payload.set.setNumber }) {
            return rejected(state, mutation, WorkoutMutationRejection.SET_NUMBER_CONFLICT)
        }

        val existingIndex = exercise.sets.indexOfFirst { it.id == payload.set.id }
        val sets = exercise.sets.toMutableList().apply {
            if (existingIndex >= 0) {
                set(existingIndex, payload.set)
            } else {
                add(payload.set)
            }
        }.sortedWith(compareBy<WorkoutSet> { it.setNumber }.thenBy { it.id })

        val exercises = session.exercises.map { candidate ->
            if (candidate.id == payload.exerciseEntryId) candidate.copy(sets = sets) else candidate
        }
        return applied(state, mutation, session.copy(exercises = exercises))
    }

    private fun removeSet(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: SetRemoved
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        val exercise = session.exercises.firstOrNull { it.id == payload.exerciseEntryId }
            ?: return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_NOT_FOUND)
        if (exercise.sets.none { it.id == payload.setId }) {
            return rejected(state, mutation, WorkoutMutationRejection.SET_NOT_FOUND)
        }

        val exercises = session.exercises.map { candidate ->
            if (candidate.id == payload.exerciseEntryId) {
                candidate.copy(sets = candidate.sets.filterNot { it.id == payload.setId })
            } else {
                candidate
            }
        }
        val timer = if (state.restTimer.setId == payload.setId) RestTimerState() else state.restTimer
        return applied(state, mutation, session.copy(exercises = exercises), timer)
    }

    private fun updateSessionNotes(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: SessionNotesUpdated
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        return applied(state, mutation, session.copy(notes = payload.notes))
    }

    private fun startRestTimer(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: RestTimerStarted
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        val exercise = session.exercises.firstOrNull { it.id == payload.exerciseEntryId }
            ?: return rejected(state, mutation, WorkoutMutationRejection.EXERCISE_NOT_FOUND)
        if (exercise.isSkipped || payload.durationSeconds !in 1..MAX_REST_SECONDS) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER)
        }
        if (payload.setId != null && exercise.sets.none { it.id == payload.setId && it.isCompleted }) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER)
        }

        val durationMs = payload.durationSeconds.toLong() * 1_000L
        val endsAtMs = safeAdd(mutation.occurredAtMs, durationMs)
            ?: return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER)
        val timer = RestTimerState(
            status = RestTimerStatus.RUNNING,
            exerciseEntryId = payload.exerciseEntryId,
            setId = payload.setId,
            durationSeconds = payload.durationSeconds,
            startedAtMs = mutation.occurredAtMs,
            endsAtMs = endsAtMs
        )
        return applied(state, mutation, session, timer)
    }

    private fun adjustRestTimer(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: RestTimerAdjusted
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        val current = state.restTimer
        if (current.status != RestTimerStatus.RUNNING || current.endsAtMs == null || payload.deltaSeconds == 0) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER_STATE)
        }

        val newDuration = current.durationSeconds.toLong() + payload.deltaSeconds.toLong()
        if (newDuration !in 0L..MAX_REST_SECONDS.toLong()) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER)
        }
        val adjustedEnd = safeAdd(current.endsAtMs, payload.deltaSeconds.toLong() * 1_000L)
            ?: return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER)
        val completed = newDuration == 0L || adjustedEnd <= mutation.occurredAtMs
        val timer = current.copy(
            status = if (completed) RestTimerStatus.COMPLETED else RestTimerStatus.RUNNING,
            durationSeconds = newDuration.toInt(),
            endsAtMs = adjustedEnd
        )
        return applied(state, mutation, session, timer)
    }

    private fun stopRestTimer(
        state: WorkoutEngineState,
        mutation: WorkoutMutation
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        if (state.restTimer.status == RestTimerStatus.IDLE) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_TIMER_STATE)
        }
        return applied(state, mutation, session, RestTimerState())
    }

    private fun finishSession(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: SessionFinished
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        if (payload.endedAtMs < session.startedAtMs) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_END_TIME)
        }
        return applied(
            state = state,
            mutation = mutation,
            session = session.copy(
                endedAtMs = payload.endedAtMs,
                status = WorkoutSessionStatus.COMPLETED
            ),
            restTimer = RestTimerState()
        )
    }

    private fun discardSession(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        payload: SessionDiscarded
    ): WorkoutReductionResult {
        val session = activeSessionOrReject(state, mutation) ?: return activeSessionRejection(state, mutation)
        if (payload.endedAtMs < session.startedAtMs) {
            return rejected(state, mutation, WorkoutMutationRejection.INVALID_END_TIME)
        }
        return applied(
            state = state,
            mutation = mutation,
            session = session.copy(
                endedAtMs = payload.endedAtMs,
                status = WorkoutSessionStatus.DISCARDED
            ),
            restTimer = RestTimerState()
        )
    }

    private fun activeSessionOrReject(
        state: WorkoutEngineState,
        mutation: WorkoutMutation
    ): WorkoutSession? {
        val session = state.session ?: return null
        if (!session.isActive || session.id != mutation.sessionId) return null
        return session
    }

    private fun activeSessionRejection(
        state: WorkoutEngineState,
        mutation: WorkoutMutation
    ): WorkoutReductionResult = when {
        state.session == null -> rejected(state, mutation, WorkoutMutationRejection.NO_ACTIVE_SESSION)
        !state.session.isActive -> rejected(state, mutation, WorkoutMutationRejection.SESSION_NOT_ACTIVE)
        state.session.id != mutation.sessionId ->
            rejected(state, mutation, WorkoutMutationRejection.SESSION_ID_MISMATCH)
        else -> rejected(state, mutation, WorkoutMutationRejection.INVALID_MUTATION)
    }

    private fun applied(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        session: WorkoutSession,
        restTimer: RestTimerState = state.restTimer
    ): WorkoutReductionResult.Applied = WorkoutReductionResult.Applied(
        state = state.copy(
            session = session.copy(revision = session.revision + 1L),
            restTimer = restTimer
        ),
        mutationId = mutation.mutationId
    )

    private fun rejected(
        state: WorkoutEngineState,
        mutation: WorkoutMutation,
        reason: WorkoutMutationRejection
    ): WorkoutReductionResult.Rejected = WorkoutReductionResult.Rejected(
        state = state,
        mutationId = mutation.mutationId,
        reason = reason
    )

    private fun List<WorkoutExercise>.areValidExercises(): Boolean {
        if (map { it.id }.distinct().size != size) return false
        val allSetIds = flatMap { it.sets }.map { it.id }
        if (allSetIds.distinct().size != allSetIds.size) return false
        return all { it.isValidExercise() }
    }

    private fun WorkoutExercise.isValidExercise(): Boolean {
        if (
            id.isBlank() ||
            exerciseId.isBlank() ||
            exerciseNameSnapshot.isBlank() ||
            restSeconds !in 0..MAX_REST_SECONDS
        ) {
            return false
        }
        if (sets.any { it.exerciseEntryId != id || !it.isValidSet() }) return false
        if (sets.map { it.id }.distinct().size != sets.size) return false
        return sets.map { it.setNumber }.distinct().size == sets.size
    }

    private fun WorkoutSet.isValidSet(): Boolean {
        if (schemaVersion != WORKOUT_CONTRACT_VERSION || id.isBlank() || exerciseEntryId.isBlank()) return false
        if (setNumber <= 0 || reps?.let { it < 0 } == true || durationSeconds?.let { it < 0 } == true) return false
        if (weightKg?.let { !it.isFinite() || it < 0.0 } == true) return false
        if (distanceMeters?.let { !it.isFinite() || it < 0.0 } == true) return false
        if (rpe?.let { it !in 1..10 } == true) return false
        if (completedAtMs?.let { it < 0L } == true) return false
        if (isCompleted && (!hasRecordedMetric || completedAtMs == null)) return false
        if (!isCompleted && completedAtMs != null) return false
        return true
    }

    private fun List<WorkoutExercise>.normalizedOrders(): List<WorkoutExercise> =
        mapIndexed { index, exercise ->
            exercise.copy(
                order = index,
                sets = exercise.sets.sortedWith(compareBy<WorkoutSet> { it.setNumber }.thenBy { it.id })
            )
        }

    private fun safeAdd(base: Long, delta: Long): Long? {
        if (delta > 0L && base > Long.MAX_VALUE - delta) return null
        if (delta < 0L && base < Long.MIN_VALUE - delta) return null
        return base + delta
    }
}
