package com.tnyx.features.profile.presentation.home

data class ProfileHomeUiState(
    val displayName: String = "Arjun Verma",
    val status: String = "Keep going! You're doing great 💪",
    val planLabel: String = "Pro Plan",
    val streak: Int = 28,
    val weight: Double = 78.4,
    val bodyFat: Double = 12.5,
    val height: Int = 182,
    val bmi: Double = 23.6,
    val bmr: Int = 1840,
    val currentJourney: CurrentJourneyState = CurrentJourneyState(),
    val progressPhotos: List<String> = listOf(), // Image URLs or resource names
    val lastPhotoUpdateWeight: String = "78.4 kg",
    val lastPhotoUpdateDate: String = "24 Dec"
)

data class CurrentJourneyState(
    val name: String = "Gain Muscle",
    val initialWeight: Double = 70.0,
    val targetWeight: Double = 70.5,
    val progress: Float = 0.2f // 0.0 to 1.0
)

sealed interface ProfileHomeAction {
    data object BackClicked : ProfileHomeAction
    data object SupportClicked : ProfileHomeAction
    data object SettingsClicked : ProfileHomeAction
    data object ViewAllProgressClicked : ProfileHomeAction
    data object JourneyHistoryClicked : ProfileHomeAction
    data object ProgressPhotosClicked : ProfileHomeAction
    data object AddProgressPhotosClicked : ProfileHomeAction
    data object NutritionTargetsClicked : ProfileHomeAction
    data object WorkoutSettingsClicked : ProfileHomeAction
    data object HealthConnectionsClicked : ProfileHomeAction
    data object GraphSettingsClicked : ProfileHomeAction
    data object RewardsClicked : ProfileHomeAction
    data object ResourcesClicked : ProfileHomeAction
}
