package com.tnyx.features.workout.data.repository

import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.features.workout.domain.repository.CustomExerciseMediaUpdate
import com.tnyx.shared.workout.domain.catalog.ExerciseCatalogParser
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalExerciseCatalogRepository @Inject constructor() : ExerciseCatalogRepository {

    private val exercisesState = MutableStateFlow<List<ExerciseDefinition>>(emptyList())

    init {
        val loaded = ExerciseCatalogParser.loadFromResources()
        exercisesState.value = loaded
    }

    override fun getExercises(): Flow<List<ExerciseDefinition>> = exercisesState.asStateFlow()

    override fun searchExercises(query: String, muscleGroupFilter: String): Flow<List<ExerciseDefinition>> {
        return exercisesState.map { list ->
            list.filter { exercise ->
                val matchesQuery = query.isBlank() ||
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.primaryMuscleGroups.any { it.contains(query, ignoreCase = true) } ||
                    exercise.equipment.any { it.contains(query, ignoreCase = true) }

                val matchesMuscle = muscleGroupFilter.equals("ALL", ignoreCase = true) ||
                    exercise.primaryMuscleGroups.any { it.equals(muscleGroupFilter, ignoreCase = true) }

                matchesQuery && matchesMuscle
            }
        }
    }

    override suspend fun getExerciseById(exerciseId: String): ExerciseDefinition? {
        return exercisesState.value.find { exercise -> exercise.id == exerciseId }
    }

    override suspend fun saveCustomExercise(
        exercise: ExerciseDefinition,
        mediaUpdate: CustomExerciseMediaUpdate,
    ) {
        exercisesState.update { current ->
            val customExercise = exercise.copy(isCustom = true)
            listOf(customExercise) + current.filterNot { existing -> existing.id == customExercise.id }
        }
    }

    override suspend fun deleteCustomExercise(exerciseId: String) {
        exercisesState.update { current ->
            current.filterNot { exercise -> exercise.id == exerciseId && exercise.isCustom }
        }
    }
}
