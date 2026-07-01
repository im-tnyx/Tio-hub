package com.tnyx.features.profile.presentation.home

data class ProfileHomeUiState(
    // User Profile Card
    val displayName: String = "Active Explorer",
    val planLabel: String = "FREE PLAN",
    val weight: Double = 70.0,
    val height: Int = 175,
    val bmi: Double = 22.5,
    val bmiStatus: BmiStatus = BmiStatus.Healthy,
    val bmr: Int = 1650,
    
    // Stats Grid (2x2)
    val workoutHistory: WorkoutHistoryStats = WorkoutHistoryStats(),
    val calendarStats: CalendarStats = CalendarStats(),
    val overallStats: OverallStats = OverallStats(),
    val exerciseLibrary: ExerciseLibraryStats = ExerciseLibraryStats(),
    
    // Current Journey
    val currentJourney: CurrentJourneyState = CurrentJourneyState(),
    
    // Progress Photos
    val progressPhotos: List<String> = listOf(),
    
    // Loading state
    val isLoading: Boolean = false
)

// BMI Status with health indicator
enum class BmiStatus(val label: String, val isHealthy: Boolean) {
    Underweight("Underweight", false),
    Healthy("Healthy", true),
    Overweight("Overweight", false),
    Obese("Obese", false);
    
    companion object {
        fun fromBmi(bmi: Double): BmiStatus = when {
            bmi < 18.5 -> Underweight
            bmi < 25.0 -> Healthy
            bmi < 30.0 -> Overweight
            else -> Obese
        }
    }
}

// Stats Grid Data Models
data class WorkoutHistoryStats(
    val weeklyCount: Int = 7,
    val currentStreak: Int = 5
)

data class CalendarStats(
    val nextWorkout: String? = "Leg Day",
    val nextDate: String? = "Tomorrow 6:00 AM"
)

data class OverallStats(
    val totalWorkouts: Int = 42,
    val totalCalories: Int = 12500
)

data class ExerciseLibraryStats(
    val totalExercises: Int = 24,
    val favoriteCount: Int = 8
)

data class CurrentJourneyState(
    val name: String = "Gain Muscle",
    val initialWeight: Double = 70.0,
    val targetWeight: Double = 75.0,
    val currentWeight: Double = 70.0,
    val progress: Float = 0.7f // 0.0 to 1.0
)

sealed interface ProfileHomeAction {
    data object BackClicked : ProfileHomeAction
    data object SupportClicked : ProfileHomeAction
    data object SettingsClicked : ProfileHomeAction
    
    // Stats Grid Actions
    data object ViewWorkoutHistoryClicked : ProfileHomeAction
    data object ViewCalendarClicked : ProfileHomeAction
    data object ViewStatisticsClicked : ProfileHomeAction
    data object ViewExercisesClicked : ProfileHomeAction
    
    // Journey & Photos
    data object JourneyHistoryClicked : ProfileHomeAction
    data object AddProgressPhotosClicked : ProfileHomeAction
    
    // Quick Actions
    data object NutritionTargetsClicked : ProfileHomeAction
    data object WorkoutSettingsClicked : ProfileHomeAction
    data object GraphSettingsClicked : ProfileHomeAction
    data object WearOsSettingsClicked : ProfileHomeAction
    
    // More
    data object ResourcesClicked : ProfileHomeAction
    data object RewardsClicked : ProfileHomeAction
    
    // Health Connections
    data object HealthConnectionsClicked : ProfileHomeAction
}
