package com.tnyx.di

import android.content.Context
import androidx.room.Room
import com.tnyx.data.workout.RoomWorkoutRepository
import com.tnyx.data.workout.WorkoutPersistenceCodec
import com.tnyx.data.workout.local.WorkoutDao
import com.tnyx.data.workout.local.WorkoutDatabase
import com.tnyx.shared.workout.domain.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkoutDataModule {
    @Provides
    @Singleton
    fun provideWorkoutDatabase(
        @ApplicationContext context: Context
    ): WorkoutDatabase = Room.databaseBuilder(
        context,
        WorkoutDatabase::class.java,
        WorkoutDatabase.NAME
    ).build()

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
}
