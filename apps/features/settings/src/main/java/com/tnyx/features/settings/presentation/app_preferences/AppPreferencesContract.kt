package com.tnyx.features.settings.presentation.app_preferences

import androidx.compose.runtime.Immutable

@Immutable
data class AppPreferencesUiState(
    val notificationsEnabled: Boolean = false,
    val wakingTime: String = "06:00 AM",
    val sleepingTime: String = "10:00 PM",
    val frequency: Int = 5,
    val remindersEnabled: Boolean = true,
    val remindersExpanded: Boolean = false,
    val nutritionReminders: Boolean = true,
    val workoutReminders: Boolean = true,
    val hydrationReminders: Boolean = true,
    val recoveryReminders: Boolean = true,
    val routinesReminders: Boolean = true,
    val theme: String = "Dark",
    val language: String = "English",
    val weightUnit: String = "kg",
    val distanceUnit: String = "kilometers",
    val bodyUnit: String = "cm",
    val soundEffects: Boolean = true,
    val soundVolume: Float = 0.8f,
    val firstDayOfWeek: String = "Sunday",
    val showFirstDayOfWeekBottomSheet: Boolean = false,
) {
    val unitSystemSummary: String
        get() = when {
            weightUnit == "kg" && distanceUnit == "kilometers" && bodyUnit == "cm" -> "Metric"
            weightUnit == "lbs" && distanceUnit == "miles" && bodyUnit == "in" -> "Imperial"
            else -> "Custom"
        }
}

sealed interface AppPreferencesAction {
    data object BackClicked : AppPreferencesAction
    data object BottomNavigationClicked : AppPreferencesAction
    data object NotificationsToggled : AppPreferencesAction
    data object RemindersToggled : AppPreferencesAction
    data object RemindersExpandedToggled : AppPreferencesAction
    data object NutritionRemindersToggled : AppPreferencesAction
    data object WorkoutRemindersToggled : AppPreferencesAction
    data object HydrationRemindersToggled : AppPreferencesAction
    data object RecoveryRemindersToggled : AppPreferencesAction
    data object RoutinesRemindersToggled : AppPreferencesAction
    data object SoundEffectsToggled : AppPreferencesAction
    data class SoundVolumeChanged(val value: Float) : AppPreferencesAction
    data object FirstDayOfWeekClicked : AppPreferencesAction
    data class FirstDayOfWeekSelected(val day: String) : AppPreferencesAction
    data object FirstDayOfWeekBottomSheetDismissed : AppPreferencesAction
}
