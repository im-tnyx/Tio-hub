package com.tnyx.features.settings.presentation.app_preferences

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppPreferencesViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AppPreferencesUiState())
    val uiState = _uiState.asStateFlow()

    fun handleAction(action: AppPreferencesAction) {
        when (action) {
            AppPreferencesAction.BackClicked,
            AppPreferencesAction.BottomNavigationClicked -> Unit
            AppPreferencesAction.NotificationsToggled -> {
                _uiState.value = _uiState.value.copy(
                    notificationsEnabled = !_uiState.value.notificationsEnabled
                )
            }
            AppPreferencesAction.RemindersToggled -> {
                _uiState.value = _uiState.value.copy(
                    remindersEnabled = !_uiState.value.remindersEnabled
                )
            }
            AppPreferencesAction.RemindersExpandedToggled -> {
                _uiState.value = _uiState.value.copy(
                    remindersExpanded = !_uiState.value.remindersExpanded
                )
            }
            AppPreferencesAction.NutritionRemindersToggled -> {
                _uiState.value = _uiState.value.copy(
                    nutritionReminders = !_uiState.value.nutritionReminders
                )
            }
            AppPreferencesAction.WorkoutRemindersToggled -> {
                _uiState.value = _uiState.value.copy(
                    workoutReminders = !_uiState.value.workoutReminders
                )
            }
            AppPreferencesAction.HydrationRemindersToggled -> {
                _uiState.value = _uiState.value.copy(
                    hydrationReminders = !_uiState.value.hydrationReminders
                )
            }
            AppPreferencesAction.RecoveryRemindersToggled -> {
                _uiState.value = _uiState.value.copy(
                    recoveryReminders = !_uiState.value.recoveryReminders
                )
            }
            AppPreferencesAction.RoutinesRemindersToggled -> {
                _uiState.value = _uiState.value.copy(
                    routinesReminders = !_uiState.value.routinesReminders
                )
            }
            AppPreferencesAction.SoundEffectsToggled -> {
                _uiState.value = _uiState.value.copy(
                    soundEffects = !_uiState.value.soundEffects
                )
            }
            is AppPreferencesAction.SoundVolumeChanged -> {
                _uiState.value = _uiState.value.copy(soundVolume = action.value)
            }
        }
    }
}
