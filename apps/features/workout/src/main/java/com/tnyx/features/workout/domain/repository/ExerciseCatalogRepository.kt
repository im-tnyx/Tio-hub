package com.tnyx.features.workout.domain.repository

import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlinx.coroutines.flow.Flow

interface ExerciseCatalogRepository {
    fun getExercises(): Flow<List<ExerciseDefinition>>
    fun searchExercises(query: String, muscleGroupFilter: String = "ALL"): Flow<List<ExerciseDefinition>>
    suspend fun getExerciseById(exerciseId: String): ExerciseDefinition?
    suspend fun saveCustomExercise(
        exercise: ExerciseDefinition,
        mediaUpdate: CustomExerciseMediaUpdate = CustomExerciseMediaUpdate.Unchanged,
    )
    suspend fun deleteCustomExercise(exerciseId: String)
}

sealed interface CustomExerciseMediaUpdate {
    data object Unchanged : CustomExerciseMediaUpdate
    data object Remove : CustomExerciseMediaUpdate

    data class Replace(
        val localFilePath: String,
        val mimeType: String,
    ) : CustomExerciseMediaUpdate {
        init {
            require(localFilePath.isNotBlank()) { "Exercise media file path is empty" }
            require(mimeType.isNotBlank()) { "Exercise media MIME type is empty" }
        }
    }
}
