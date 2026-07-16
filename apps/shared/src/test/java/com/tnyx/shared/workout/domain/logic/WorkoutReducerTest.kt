package com.tnyx.shared.workout.domain.logic

import com.tnyx.shared.workout.domain.model.ExerciseAdded
import com.tnyx.shared.workout.domain.model.ExerciseReordered
import com.tnyx.shared.workout.domain.model.RestTimerAdjusted
import com.tnyx.shared.workout.domain.model.RestTimerStarted
import com.tnyx.shared.workout.domain.model.RestTimerStatus
import com.tnyx.shared.workout.domain.model.RestTimerStopped
import com.tnyx.shared.workout.domain.model.SessionFinished
import com.tnyx.shared.workout.domain.model.SessionStarted
import com.tnyx.shared.workout.domain.model.SetUpserted
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutExercise
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutMutationOrigin
import com.tnyx.shared.workout.domain.model.WorkoutMutationPayload
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.model.WorkoutSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorkoutReducerTest {
    @Test
    fun thinOfflineFlowProducesOneCompletedSession() {
        var state = apply(
            WorkoutEngineState(),
            mutation(1, SessionStarted(startedAtMs = 1_000L), occurredAtMs = 1_000L)
        )
        state = apply(
            state,
            mutation(2, ExerciseAdded(exercise("entry-1", "exercise-1", 0)))
        )
        state = apply(
            state,
            mutation(
                sequence = 3,
                payload = SetUpserted(
                    exerciseEntryId = "entry-1",
                    set = completedSet("set-1", "entry-1", 1)
                ),
                occurredAtMs = 2_000L
            )
        )
        state = apply(
            state,
            mutation(4, SessionFinished(endedAtMs = 3_000L), occurredAtMs = 3_000L)
        )

        val session = requireNotNull(state.session)
        assertEquals(WorkoutSessionStatus.COMPLETED, session.status)
        assertEquals(3_000L, session.endedAtMs)
        assertEquals(1, session.completedSets)
        assertEquals(1, session.workingSets)
        assertEquals(600.0, session.totalVolumeKg)
        assertEquals(4L, session.revision)
        assertEquals(RestTimerStatus.IDLE, state.restTimer.status)
    }

    @Test
    fun secondStartWhileActiveIsRejectedWithoutChangingState() {
        val active = apply(
            WorkoutEngineState(),
            mutation(1, SessionStarted(startedAtMs = 1_000L), occurredAtMs = 1_000L)
        )

        val result = WorkoutReducer.reduce(
            active,
            mutation(2, SessionStarted(startedAtMs = 2_000L), occurredAtMs = 2_000L)
        )

        val rejected = assertIs<WorkoutReductionResult.Rejected>(result)
        assertEquals(WorkoutMutationRejection.SESSION_ALREADY_ACTIVE, rejected.reason)
        assertEquals(active, rejected.state)
    }

    @Test
    fun invalidCompletedSetIsRejectedWithoutChangingState() {
        var state = apply(
            WorkoutEngineState(),
            mutation(1, SessionStarted(startedAtMs = 1_000L), occurredAtMs = 1_000L)
        )
        state = apply(state, mutation(2, ExerciseAdded(exercise("entry-1", "exercise-1", 0))))

        val result = WorkoutReducer.reduce(
            state,
            mutation(
                3,
                SetUpserted(
                    exerciseEntryId = "entry-1",
                    set = WorkoutSet(
                        id = "invalid-set",
                        exerciseEntryId = "entry-1",
                        setNumber = 1,
                        reps = 10,
                        isCompleted = true,
                        completedAtMs = null
                    )
                )
            )
        )

        val rejected = assertIs<WorkoutReductionResult.Rejected>(result)
        assertEquals(WorkoutMutationRejection.INVALID_SET, rejected.reason)
        assertEquals(state, rejected.state)
    }

    @Test
    fun reorderNormalizesExerciseOrder() {
        val initialExercises = listOf(
            exercise("entry-a", "exercise-a", 8),
            exercise("entry-b", "exercise-b", 2)
        )
        var state = apply(
            WorkoutEngineState(),
            mutation(1, SessionStarted(startedAtMs = 1_000L, initialExercises = initialExercises))
        )
        assertEquals(listOf(0, 1), state.session?.exercises?.map { it.order })

        state = apply(state, mutation(2, ExerciseReordered("entry-a", toIndex = 1)))

        assertEquals(listOf("entry-b", "entry-a"), state.session?.exercises?.map { it.id })
        assertEquals(listOf(0, 1), state.session?.exercises?.map { it.order })
    }

    @Test
    fun restTimerStartsAdjustsAndStopsDeterministically() {
        val entry = exercise("entry-1", "exercise-1", 0).copy(
            sets = listOf(completedSet("set-1", "entry-1", 1))
        )
        var state = apply(
            WorkoutEngineState(),
            mutation(
                1,
                SessionStarted(startedAtMs = 1_000L, initialExercises = listOf(entry)),
                occurredAtMs = 1_000L
            )
        )
        state = apply(
            state,
            mutation(
                2,
                RestTimerStarted("entry-1", setId = "set-1", durationSeconds = 90),
                occurredAtMs = 2_000L
            )
        )
        assertEquals(92_000L, state.restTimer.endsAtMs)
        assertEquals(90, state.restTimer.durationSeconds)

        state = apply(
            state,
            mutation(3, RestTimerAdjusted(deltaSeconds = -30), occurredAtMs = 3_000L)
        )
        assertEquals(62_000L, state.restTimer.endsAtMs)
        assertEquals(60, state.restTimer.durationSeconds)
        assertEquals(59, state.restTimer.remainingSeconds(3_000L))

        state = apply(state, mutation(4, RestTimerStopped, occurredAtMs = 4_000L))
        assertEquals(RestTimerStatus.IDLE, state.restTimer.status)
    }

    @Test
    fun mutationForDifferentSessionIsRejected() {
        val state = apply(
            WorkoutEngineState(),
            mutation(1, SessionStarted(startedAtMs = 1_000L), occurredAtMs = 1_000L)
        )

        val result = WorkoutReducer.reduce(
            state,
            mutation(
                sequence = 2,
                payload = ExerciseAdded(exercise("entry-1", "exercise-1", 0)),
                sessionId = "another-session"
            )
        )

        val rejected = assertIs<WorkoutReductionResult.Rejected>(result)
        assertEquals(WorkoutMutationRejection.SESSION_ID_MISMATCH, rejected.reason)
        assertTrue(rejected.state.session?.exercises?.isEmpty() == true)
    }

    @Test
    fun addingExerciseWithExistingSetIdIsRejected() {
        val firstEntry = exercise("entry-1", "exercise-1", 0).copy(
            sets = listOf(completedSet("shared-set-id", "entry-1", 1))
        )
        val state = apply(
            WorkoutEngineState(),
            mutation(1, SessionStarted(startedAtMs = 1_000L, initialExercises = listOf(firstEntry)))
        )
        val secondEntry = exercise("entry-2", "exercise-2", 1).copy(
            sets = listOf(completedSet("shared-set-id", "entry-2", 1))
        )

        val result = WorkoutReducer.reduce(
            state,
            mutation(2, ExerciseAdded(secondEntry))
        )

        val rejected = assertIs<WorkoutReductionResult.Rejected>(result)
        assertEquals(WorkoutMutationRejection.SET_ID_CONFLICT, rejected.reason)
        assertEquals(state, rejected.state)
    }

    private fun apply(
        state: WorkoutEngineState,
        mutation: WorkoutMutation
    ): WorkoutEngineState = assertIs<WorkoutReductionResult.Applied>(
        WorkoutReducer.reduce(state, mutation)
    ).state

    private fun mutation(
        sequence: Long,
        payload: WorkoutMutationPayload,
        occurredAtMs: Long = 1_000L + sequence,
        sessionId: String = "session-1"
    ): WorkoutMutation = WorkoutMutation(
        mutationId = "mutation-$sequence",
        sessionId = sessionId,
        origin = WorkoutMutationOrigin.PHONE,
        originDeviceId = "phone-device",
        originSequence = sequence,
        occurredAtMs = occurredAtMs,
        payload = payload
    )

    private fun exercise(
        entryId: String,
        exerciseId: String,
        order: Int
    ): WorkoutExercise = WorkoutExercise(
        id = entryId,
        exerciseId = exerciseId,
        exerciseNameSnapshot = "Exercise $exerciseId",
        order = order
    )

    private fun completedSet(
        id: String,
        exerciseEntryId: String,
        setNumber: Int
    ): WorkoutSet = WorkoutSet(
        id = id,
        exerciseEntryId = exerciseEntryId,
        setNumber = setNumber,
        weightKg = 60.0,
        reps = 10,
        isCompleted = true,
        completedAtMs = 2_000L
    )
}
