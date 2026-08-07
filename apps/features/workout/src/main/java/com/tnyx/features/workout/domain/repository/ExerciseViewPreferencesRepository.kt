package com.tnyx.features.workout.domain.repository

import com.tnyx.features.workout.presentation.library.exercises.ExerciseViewType
import kotlinx.coroutines.flow.Flow

interface ExerciseViewPreferencesRepository {
    val viewType: Flow<ExerciseViewType>
    suspend fun saveViewType(viewType: ExerciseViewType)
}
