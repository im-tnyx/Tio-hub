package com.tnyx.shared.workout.domain.catalog

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ExerciseCatalogParserTest {

    @Test
    fun testLoadFromResourcesParsesExercisesSuccessfully() {
        val exercises = ExerciseCatalogParser.loadFromResources()
        assertTrue(exercises.isNotEmpty(), "Exercise catalog loaded from resources should not be empty")

        val benchPress = exercises.find { it.id == "79D0BB3A" || it.name.contains("Bench Press", ignoreCase = true) }
        assertTrue(benchPress != null, "Bench Press exercise should be present in catalog")
        assertEquals("chest", benchPress.primaryMuscleGroups.firstOrNull())
        assertTrue(benchPress.mediaAssets.isNotEmpty(), "Bench press should have media assets mapped")
    }
}
