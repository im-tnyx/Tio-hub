package com.tnyx.data.workout.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkoutEngineStateEntity::class,
        WorkoutMutationOutboxEntity::class,
        WorkoutSessionHistoryEntity::class,
        WorkoutExerciseDefinitionEntity::class,
        WorkoutRoutineEntity::class
    ],
    version = WorkoutDatabase.VERSION,
    exportSchema = true
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        const val VERSION: Int = 1
        const val NAME: String = "tnyx-workout.db"
    }
}
