package com.tnyx.features.workout.domain.repository

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlinx.coroutines.flow.Flow

interface ExerciseCatalogRepository {
    fun getExercises(): Flow<List<ExerciseDefinition>>
    fun searchExercises(query: String, muscleGroupFilter: String = "ALL"): Flow<List<ExerciseDefinition>>
    suspend fun getExerciseById(exerciseId: String): ExerciseDefinition?
    suspend fun saveCustomExercise(exercise: ExerciseDefinition)
    suspend fun deleteCustomExercise(exerciseId: String)
}
