package com.tnyx.di

import com.tnyx.data.preferences.DataStoreExerciseViewPreferencesRepository
import com.tnyx.features.workout.domain.repository.ExerciseViewPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseViewPreferencesModule {

    @Binds
    @Singleton
    abstract fun bindExerciseViewPreferencesRepository(
        implementation: DataStoreExerciseViewPreferencesRepository,
    ): ExerciseViewPreferencesRepository
}
