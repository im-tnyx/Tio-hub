package com.tnyx.data.workout.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_engine_state WHERE singletonId = 1 LIMIT 1")
    fun observeEngineState(): Flow<WorkoutEngineStateEntity?>

    @Query("SELECT * FROM workout_engine_state WHERE singletonId = 1 LIMIT 1")
    suspend fun getEngineState(): WorkoutEngineStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEngineState(entity: WorkoutEngineStateEntity)

    @Query("SELECT * FROM workout_mutation_outbox WHERE mutationId = :mutationId LIMIT 1")
    suspend fun getMutationById(mutationId: String): WorkoutMutationOutboxEntity?

    @Query(
        """
        SELECT MAX(originSequence)
        FROM workout_mutation_outbox
        WHERE origin = :origin AND originDeviceId = :originDeviceId
        """
    )
    suspend fun getLatestOriginSequence(
        origin: String,
        originDeviceId: String
    ): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMutation(entity: WorkoutMutationOutboxEntity)

    @Query("SELECT COUNT(*) FROM workout_mutation_outbox")
    suspend fun getMutationCount(): Int

    @Query(
        """
        SELECT * FROM workout_session_history
        ORDER BY endedAtMs DESC, startedAtMs DESC
        """
    )
    fun observeSessionHistory(): Flow<List<WorkoutSessionHistoryEntity>>

    @Query("SELECT * FROM workout_session_history WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionHistoryById(sessionId: String): WorkoutSessionHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionHistory(entity: WorkoutSessionHistoryEntity)

    @Query("SELECT * FROM workout_exercise_definition ORDER BY name COLLATE NOCASE, exerciseId")
    fun observeExerciseDefinitions(): Flow<List<WorkoutExerciseDefinitionEntity>>

    @Query("SELECT * FROM workout_exercise_definition WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getExerciseDefinitionById(exerciseId: String): WorkoutExerciseDefinitionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExerciseDefinitions(entities: List<WorkoutExerciseDefinitionEntity>)

    @Query("SELECT * FROM workout_routine ORDER BY name COLLATE NOCASE, routineId")
    fun observeRoutines(): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routine WHERE routineId = :routineId LIMIT 1")
    suspend fun getRoutineById(routineId: String): WorkoutRoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutines(entities: List<WorkoutRoutineEntity>)
}
