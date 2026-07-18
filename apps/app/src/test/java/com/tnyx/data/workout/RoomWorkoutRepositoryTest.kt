package com.tnyx.data.workout

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.tnyx.data.workout.local.WorkoutDatabase
import com.tnyx.shared.workout.domain.logic.WorkoutMutationRejection
import com.tnyx.shared.workout.domain.model.SessionDiscarded
import com.tnyx.shared.workout.domain.model.SessionFinished
import com.tnyx.shared.workout.domain.model.SessionNotesUpdated
import com.tnyx.shared.workout.domain.model.SessionStarted
import com.tnyx.shared.workout.domain.model.SetUpserted
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutExercise
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutMutationOrigin
import com.tnyx.shared.workout.domain.model.WorkoutMutationPayload
import com.tnyx.shared.workout.domain.model.WorkoutSessionStatus
import com.tnyx.shared.workout.domain.model.WorkoutSet
import com.tnyx.shared.workout.domain.repository.WorkoutMutationApplyResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class RoomWorkoutRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: WorkoutDatabase
    private lateinit var codec: WorkoutPersistenceCodec
    private lateinit var repository: RoomWorkoutRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        codec = WorkoutPersistenceCodec()
        repository = repositoryFor(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun activeSessionAndLastCompletedSetSurviveDatabaseReopen() = runBlocking {
        val databaseName = "workout-recovery-${UUID.randomUUID()}.db"
        context.deleteDatabase(databaseName)
        val firstDatabase = openFileDatabase(databaseName)
        val firstRepository = repositoryFor(firstDatabase)

        assertApplied(firstRepository.applyMutation(startMutation(sequence = 1L)))
        assertApplied(
            firstRepository.applyMutation(
                mutation(
                    sequence = 2L,
                    payload = SetUpserted(
                        exerciseEntryId = EXERCISE_ENTRY_ID,
                        set = completedSet()
                    )
                )
            )
        )
        firstDatabase.close()

        val reopenedDatabase = openFileDatabase(databaseName)
        val reopenedRepository = repositoryFor(reopenedDatabase)
        val restored = reopenedRepository.observeEngineState().first()

        assertEquals(SESSION_ID, restored.session?.id)
        assertTrue(restored.session?.isActive == true)
        assertEquals(2L, restored.session?.revision)
        assertEquals(1, restored.session?.completedSets)
        assertEquals(SET_ID, restored.session?.sets?.single()?.id)
        assertEquals(2, reopenedDatabase.workoutDao().getMutationCount())

        reopenedDatabase.close()
        context.deleteDatabase(databaseName)
        Unit
    }

    @Test
    fun nextPhoneSequenceSurvivesDatabaseReopen() = runBlocking {
        val databaseName = "workout-sequence-${UUID.randomUUID()}.db"
        context.deleteDatabase(databaseName)
        val firstDatabase = openFileDatabase(databaseName)
        val firstRepository = repositoryFor(firstDatabase)

        assertEquals(
            0L,
            firstRepository.nextMutationSequence(WorkoutMutationOrigin.PHONE, "phone-device")
        )
        assertApplied(firstRepository.applyMutation(startMutation(sequence = 0L)))
        firstDatabase.close()

        val reopenedDatabase = openFileDatabase(databaseName)
        val reopenedRepository = repositoryFor(reopenedDatabase)
        assertEquals(
            1L,
            reopenedRepository.nextMutationSequence(WorkoutMutationOrigin.PHONE, "phone-device")
        )

        reopenedDatabase.close()
        context.deleteDatabase(databaseName)
        Unit
    }

    @Test
    fun duplicateMutationIdIsIdempotent() = runBlocking {
        val start = startMutation(sequence = 1L)
        val first = repository.applyMutation(start)
        val duplicate = repository.applyMutation(start)

        assertApplied(first)
        assertTrue(duplicate is WorkoutMutationApplyResult.AlreadyApplied)
        assertEquals(1L, duplicate.state.session?.revision)
        assertEquals(1, database.workoutDao().getMutationCount())
        val persisted = database.workoutDao().getMutationById(start.mutationId)
        assertEquals(start, codec.decodeMutation(requireNotNull(persisted).mutationJson))
    }

    @Test
    fun reusedMutationIdWithDifferentPayloadIsRejected() = runBlocking {
        val start = startMutation(sequence = 1L)
        assertApplied(repository.applyMutation(start))

        val conflicting = start.copy(
            payload = SessionStarted(
                startedAtMs = 1_000L,
                routineName = "Conflicting payload"
            )
        )
        val result = repository.applyMutation(conflicting)

        assertTrue(result is WorkoutMutationApplyResult.Rejected)
        result as WorkoutMutationApplyResult.Rejected
        assertEquals(WorkoutMutationRejection.MUTATION_ID_CONFLICT, result.reason)
        assertEquals(1L, result.state.session?.revision)
        assertEquals(1, database.workoutDao().getMutationCount())
    }

    @Test
    fun equalDeviceSequenceIsRejectedWithoutStateChange() = runBlocking {
        val initial = assertApplied(repository.applyMutation(startMutation(sequence = 7L)))
        val result = repository.applyMutation(
            mutation(
                sequence = 7L,
                mutationId = "different-mutation-same-sequence",
                payload = SessionNotesUpdated("must not persist")
            )
        )

        assertTrue(result is WorkoutMutationApplyResult.Rejected)
        result as WorkoutMutationApplyResult.Rejected
        assertEquals(WorkoutMutationRejection.OUT_OF_ORDER_MUTATION, result.reason)
        assertEquals(initial, result.state)
        assertEquals("", repository.observeEngineState().first().session?.notes)
        assertEquals(1, database.workoutDao().getMutationCount())
    }

    @Test
    fun completedSessionEntersHistoryAndDiscardedSessionDoesNot() = runBlocking {
        assertApplied(repository.applyMutation(startMutation(sequence = 1L)))
        assertApplied(
            repository.applyMutation(
                mutation(
                    sequence = 2L,
                    payload = SessionFinished(endedAtMs = 2_000L)
                )
            )
        )
        assertApplied(
            repository.applyMutation(
                startMutation(
                    sequence = 3L,
                    sessionId = SECOND_SESSION_ID
                )
            )
        )
        val discardedState = assertApplied(
            repository.applyMutation(
                mutation(
                    sequence = 4L,
                    sessionId = SECOND_SESSION_ID,
                    payload = SessionDiscarded(endedAtMs = 4_000L)
                )
            )
        )

        val history = repository.observeSessionHistory().first()
        assertEquals(listOf(SESSION_ID), history.map { it.id })
        assertEquals(WorkoutSessionStatus.COMPLETED, repository.getSessionById(SESSION_ID)?.status)
        assertEquals(WorkoutSessionStatus.DISCARDED, discardedState.session?.status)
        assertFalse(discardedState.session?.isActive == true)
        assertEquals(WorkoutSessionStatus.DISCARDED, repository.getSessionById(SECOND_SESSION_ID)?.status)
    }

    @Test
    fun snapshotWriteRollsBackWhenOutboxInsertFails() = runBlocking {
        val start = startMutation(sequence = 1L)
        assertApplied(repository.applyMutation(start))
        val dao = database.workoutDao()
        val before = requireNotNull(dao.getEngineState())
        val persistedMutation = requireNotNull(dao.getMutationById(start.mutationId))
        val changedState = codec.decodeEngineState(before.stateJson).let { state ->
            state.copy(
                session = requireNotNull(state.session).copy(
                    revision = 999L,
                    notes = "must roll back"
                )
            )
        }
        val changedEntity = changedState.toEntity(codec, updatedAtMs = 9_999L)

        var transactionFailure: Throwable? = null
        try {
            database.withTransaction {
                dao.upsertEngineState(changedEntity)
                dao.insertMutation(persistedMutation)
            }
        } catch (failure: Throwable) {
            transactionFailure = failure
        }

        assertNotNull(transactionFailure)
        assertEquals(before, dao.getEngineState())
        assertEquals(1, dao.getMutationCount())
    }

    private fun openFileDatabase(name: String): WorkoutDatabase =
        Room.databaseBuilder(context, WorkoutDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()

    private fun repositoryFor(database: WorkoutDatabase): RoomWorkoutRepository =
        RoomWorkoutRepository(
            database = database,
            dao = database.workoutDao(),
            codec = WorkoutPersistenceCodec()
        )

    private fun assertApplied(result: WorkoutMutationApplyResult): WorkoutEngineState {
        assertTrue(result is WorkoutMutationApplyResult.Applied)
        return result.state
    }

    private fun startMutation(
        sequence: Long,
        sessionId: String = SESSION_ID
    ): WorkoutMutation = mutation(
        sequence = sequence,
        sessionId = sessionId,
        payload = SessionStarted(
            startedAtMs = sequence * 1_000L,
            routineName = "Recovery routine",
            initialExercises = listOf(
                WorkoutExercise(
                    id = EXERCISE_ENTRY_ID,
                    exerciseId = EXERCISE_ID,
                    exerciseNameSnapshot = "Recovery exercise",
                    order = 0
                )
            )
        )
    )

    private fun mutation(
        sequence: Long,
        payload: WorkoutMutationPayload,
        sessionId: String = SESSION_ID,
        mutationId: String = "mutation-$sequence"
    ): WorkoutMutation = WorkoutMutation(
        mutationId = mutationId,
        sessionId = sessionId,
        origin = WorkoutMutationOrigin.PHONE,
        originDeviceId = "phone-device",
        originSequence = sequence,
        occurredAtMs = sequence * 1_000L,
        payload = payload
    )

    private fun completedSet(): WorkoutSet = WorkoutSet(
        id = SET_ID,
        exerciseEntryId = EXERCISE_ENTRY_ID,
        setNumber = 1,
        weightKg = 60.0,
        reps = 10,
        isCompleted = true,
        completedAtMs = 2_000L
    )

    private companion object {
        const val SESSION_ID = "session-1"
        const val SECOND_SESSION_ID = "session-2"
        const val EXERCISE_ENTRY_ID = "entry-1"
        const val EXERCISE_ID = "exercise-1"
        const val SET_ID = "set-1"
    }
}
