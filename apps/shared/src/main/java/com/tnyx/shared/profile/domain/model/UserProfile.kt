package com.tnyx.shared.profile.domain.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val dob: String,
    val gender: String,
    val planLabel: String,
    val weight: Double,
    val height: Int,
    val bmi: Double,
    val bmr: Int,
    val statusLabel: String = "",
    val streak: Int = 0,
    val bodyFat: Double = 0.0,
    val currentJourney: ProfileJourney = ProfileJourney(),
    val progressPhotos: List<String> = emptyList(),
    val lastPhotoUpdateWeight: String = "",
    val lastPhotoUpdateDate: String = "",
    val workoutChart: ProfileWorkoutChart = ProfileWorkoutChart()
)

data class ProfileJourney(
    val name: String = "",
    val initialWeight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val progress: Float = 0f
)

data class ProfileWorkoutChart(
    val durationMinutes: List<Float> = emptyList(),
    val volumeKg: List<Float> = emptyList(),
    val reps: List<Float> = emptyList()
)
