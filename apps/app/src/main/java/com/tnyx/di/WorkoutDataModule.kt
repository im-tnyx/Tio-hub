package com.tnyx.di

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tnyx.data.workout.RoomWorkoutRepository
import com.tnyx.data.workout.SupabaseExerciseCatalogRepository
import com.tnyx.data.workout.WorkoutPersistenceCodec
import com.tnyx.data.workout.local.WorkoutDao
import com.tnyx.data.workout.local.WorkoutDatabase
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.workout.domain.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkoutDataModule {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `workout_custom_exercise` (
                    `ownerUserId` TEXT NOT NULL,
                    `exerciseId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `contractVersion` INTEGER NOT NULL,
                    `definitionJson` TEXT NOT NULL,
                    `syncedAtMs` INTEGER NOT NULL,
                    PRIMARY KEY(`ownerUserId`, `exerciseId`)
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                CREATE INDEX IF NOT EXISTS `index_workout_custom_exercise_ownerUserId`
                ON `workout_custom_exercise` (`ownerUserId`)
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideWorkoutDatabase(
        @ApplicationContext context: Context
    ): WorkoutDatabase = Room.databaseBuilder(
        context,
        WorkoutDatabase::class.java,
        WorkoutDatabase.NAME
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    fun provideWorkoutDao(database: WorkoutDatabase): WorkoutDao = database.workoutDao()

    @Provides
    @Singleton
    fun provideWorkoutPersistenceCodec(): WorkoutPersistenceCodec = WorkoutPersistenceCodec()

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        database: WorkoutDatabase,
        dao: WorkoutDao,
        codec: WorkoutPersistenceCodec
    ): WorkoutRepository = RoomWorkoutRepository(database, dao, codec)

    @Provides
    @Singleton
    fun provideExerciseCatalogRepository(
        database: WorkoutDatabase,
        dao: WorkoutDao,
        codec: WorkoutPersistenceCodec,
        sessionProvider: AuthSessionProvider,
        supabaseClient: SupabaseClient,
    ): ExerciseCatalogRepository = SupabaseExerciseCatalogRepository(
        database = database,
        dao = dao,
        codec = codec,
        sessionProvider = sessionProvider,
        supabaseClient = supabaseClient,
    )
}
