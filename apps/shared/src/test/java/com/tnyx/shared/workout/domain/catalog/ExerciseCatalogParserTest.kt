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

    @Test
    fun testParseNestedMediaFormatSuccessfully() {
        val sampleJson = """
        [
          {
            "id": "TEST_001",
            "title": "Barbell Curl",
            "muscle_group": "biceps",
            "exercise_type": "weight_reps",
            "equipment_category": "barbell",
            "media": {
              "type": "video",
              "defaultGender": "male",
              "male": {
                "videoUrl": "https://d2l9nsnmtah87f.cloudfront.net/exercise-assets/00311201-Barbell-Curl_Upper-Arms.mp4",
                "thumbnailUrl": "https://d2l9nsnmtah87f.cloudfront.net/exercise-thumbnails/00311201-Barbell-Curl_Upper-Arms_thumbnail@3x.jpg",
                "imageUrl": "https://apilyfta.com/static/GymvisualPNG/00311101-Barbell-Curl_Upper-Arms-FIX_small.png"
              },
              "female": {
                "videoUrl": "https://d2l9nsnmtah87f.cloudfront.net/exercise-assets/35921201-Barbell-Curl-(female)_Upper-Arms.mp4",
                "thumbnailUrl": "https://d2l9nsnmtah87f.cloudfront.net/exercise-thumbnails/35921201-Barbell-Curl-(female)_Upper-Arms_thumbnail_@3x.jpg",
                "imageUrl": "https://apilyfta.com/static/GymvisualPNG/35921101-Barbell-Curl-(female)_Upper-Arms_small.png"
              }
            }
          }
        ]
        """.trimIndent()

        val parsed = ExerciseCatalogParser.parseJson(sampleJson)
        assertEquals(1, parsed.size)
        val exercise = parsed.first()
        assertEquals("Barbell Curl", exercise.name)
        assertEquals(2, exercise.mediaAssets.size)

        val maleMedia = exercise.mediaAssets.find { it.id == "TEST_001_male" }
        assertTrue(maleMedia != null)
        assertEquals("https://apilyfta.com/static/GymvisualPNG/00311101-Barbell-Curl_Upper-Arms-FIX_small.png", maleMedia.imageRef)
        assertEquals("https://d2l9nsnmtah87f.cloudfront.net/exercise-thumbnails/00311201-Barbell-Curl_Upper-Arms_thumbnail@3x.jpg", maleMedia.thumbnailRef)
        assertEquals("https://d2l9nsnmtah87f.cloudfront.net/exercise-assets/00311201-Barbell-Curl_Upper-Arms.mp4", maleMedia.videoRef)
    }
}
