package com.tnyx.features.workout.di

import com.tnyx.features.workout.data.repository.LocalExerciseCatalogRepository
import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseCatalogModule {

    @Binds
    @Singleton
    abstract fun bindExerciseCatalogRepository(
        impl: LocalExerciseCatalogRepository
    ): ExerciseCatalogRepository
}
