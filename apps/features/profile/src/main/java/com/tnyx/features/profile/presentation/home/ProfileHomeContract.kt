package com.tnyx.features.profile.presentation.home

data class ProfileHomeUiState(
    val displayName: String = "",
    val status: String = "",
    val planLabel: String = "",
    val streak: Int = 0,
    val weight: Double = 0.0,
    val bodyFat: Double = 0.0,
    val height: Int = 0,
    val bmi: Double = 0.0,
    val bmr: Int = 0,
    val currentJourney: CurrentJourneyState = CurrentJourneyState(),
    val progressPhotos: List<String> = listOf(), // Image URLs or resource names
    val lastPhotoUpdateWeight: String = "",
    val lastPhotoUpdateDate: String = ""
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
    data object HealthConnectionsClicked : ProfileHomeAction
}
