package com.tnyx.features.profile.presentation.home

import com.tnyx.shared.profile.domain.model.MembershipTier

data class ProfileHomeUiState(
    val username: String = "",
    val displayName: String = "",
    val status: String = "",
    val planLabel: String = "",
    val avatarUrl: String? = null,
    val membershipTier: MembershipTier = MembershipTier.Free,
    val streak: Int = 0,
    val weight: Double = 0.0,
    val bodyFat: Double = 0.0,
    val height: Int = 0,
    val bmi: Double = 0.0,
    val bmr: Int = 0,
    val currentJourney: CurrentJourneyState = CurrentJourneyState(),
    val progressPhotos: List<String> = emptyList(),
    val lastPhotoUpdateWeight: String = "",
    val lastPhotoUpdateDate: String = "",
    val workoutChart: WorkoutChartState = WorkoutChartState(),
)

data class CurrentJourneyState(
    val name: String = "",
    val initialWeight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val progress: Float = 0f,
)

data class WorkoutChartState(
    val durationMinutes: List<Float> = emptyList(),
    val volumeKg: List<Float> = emptyList(),
    val reps: List<Float> = emptyList(),
)

sealed interface ProfileHomeAction {
    data object BackClicked : ProfileHomeAction
    data object EditProfileClicked : ProfileHomeAction
    data object SupportClicked : ProfileHomeAction
    data object SettingsClicked : ProfileHomeAction
    data object ViewAllProgressClicked : ProfileHomeAction
    data object JourneyHistoryClicked : ProfileHomeAction
    data object ProgressPhotosClicked : ProfileHomeAction
    data object AddProgressPhotosClicked : ProfileHomeAction
    data object HealthConnectionsClicked : ProfileHomeAction
    data object RefreshProfile : ProfileHomeAction
    data object AvatarClicked : ProfileHomeAction
}
