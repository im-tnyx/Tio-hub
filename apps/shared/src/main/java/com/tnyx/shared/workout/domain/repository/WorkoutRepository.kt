package com.tnyx.shared.workout.domain.repository

import com.tnyx.shared.workout.domain.logic.WorkoutMutationRejection
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.WorkoutEngineState
import com.tnyx.shared.workout.domain.model.WorkoutMutation
import com.tnyx.shared.workout.domain.model.WorkoutRoutine
import com.tnyx.shared.workout.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

sealed interface WorkoutMutationApplyResult {
    val state: WorkoutEngineState

    data class Applied(
        override val state: WorkoutEngineState
    ) : WorkoutMutationApplyResult

    data class AlreadyApplied(
        override val state: WorkoutEngineState
    ) : WorkoutMutationApplyResult

    data class Rejected(
        override val state: WorkoutEngineState,
        val reason: WorkoutMutationRejection
    ) : WorkoutMutationApplyResult
}

/** Platform-neutral Workout data boundary shared by Phone and Wear. */
interface WorkoutRepository {
    fun observeExerciseCatalog(): Flow<List<ExerciseDefinition>>

    suspend fun getExerciseDefinition(id: String): ExerciseDefinition?

    fun observeRoutines(): Flow<List<WorkoutRoutine>>

    suspend fun getRoutineById(id: String): WorkoutRoutine?

    fun observeEngineState(): Flow<WorkoutEngineState>

    /** Apply once and persist the state snapshot plus outgoing mutation atomically. */
    suspend fun applyMutation(mutation: WorkoutMutation): WorkoutMutationApplyResult

    fun observeSessionHistory(): Flow<List<WorkoutSession>>

    suspend fun getSessionById(id: String): WorkoutSession?
}
