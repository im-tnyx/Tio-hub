package com.tnyx.features.workout.di

import com.tnyx.features.workout.domain.DefaultWorkoutSessionCoordinator
import com.tnyx.features.workout.domain.SystemWorkoutRuntimeValues
import com.tnyx.features.workout.domain.WorkoutRuntimeValues
import com.tnyx.features.workout.domain.WorkoutSessionCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkoutFeatureModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutSessionCoordinator(
        implementation: DefaultWorkoutSessionCoordinator,
    ): WorkoutSessionCoordinator

    @Binds
    @Singleton
    abstract fun bindWorkoutRuntimeValues(
        implementation: SystemWorkoutRuntimeValues,
    ): WorkoutRuntimeValues
}
