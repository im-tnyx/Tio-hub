package com.tnyx.features.workout.domain.repository

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlinx.coroutines.flow.Flow

interface ExerciseCatalogRepository {
    fun getExercises(): Flow<List<ExerciseDefinition>>
    fun searchExercises(query: String, muscleGroupFilter: String = "ALL"): Flow<List<ExerciseDefinition>>
}
