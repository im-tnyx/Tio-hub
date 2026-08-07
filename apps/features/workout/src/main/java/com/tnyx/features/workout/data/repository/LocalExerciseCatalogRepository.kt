package com.tnyx.features.workout.data.repository

import com.tnyx.features.workout.domain.repository.ExerciseCatalogRepository
import com.tnyx.shared.workout.domain.catalog.ExerciseCatalogParser
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
}
