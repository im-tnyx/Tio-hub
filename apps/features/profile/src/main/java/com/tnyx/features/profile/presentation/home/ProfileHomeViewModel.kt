package com.tnyx.features.profile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileHomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                profileRepository.getCurrentProfile()
                    .collect { profile ->
                        _uiState.update { state ->
                            state.copy(
                                username = resolveProfileUsername(
                                    username = profile.username,
                                    displayName = profile.displayName,
                                ),
                                displayName = profile.displayName,
                                status = profile.statusLabel.ifBlank {
                                    formatStatus(profile.dob, profile.gender)
                                },
                                planLabel = profile.planLabel,
                                avatarUrl = profile.avatarUrl,
                                membershipTier = profile.membershipTier,
                                streak = profile.streak,
                                weight = profile.weight,
                                bodyFat = profile.bodyFat,
                                height = profile.height,
                                bmi = profile.bmi,
                                bmr = profile.bmr,
                                currentJourney = CurrentJourneyState(
                                    name = profile.currentJourney.name,
                                    initialWeight = profile.currentJourney.initialWeight,
                                    targetWeight = profile.currentJourney.targetWeight,
                                    progress = profile.currentJourney.progress,
                                ),
                                progressPhotos = profile.progressPhotos,
                                lastPhotoUpdateWeight = profile.lastPhotoUpdateWeight,
                                lastPhotoUpdateDate = profile.lastPhotoUpdateDate,
                                workoutChart = WorkoutChartState(
                                    durationMinutes = profile.workoutChart.durationMinutes,
                                    volumeKg = profile.workoutChart.volumeKg,
                                    reps = profile.workoutChart.reps,
                                ),
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = true) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = false) }
    }

    fun uploadAvatar(jpegBytes: ByteArray) {
        if (jpegBytes.isEmpty()) return
        viewModelScope.launch {
            try {
                profileRepository.updateAvatar(jpegBytes)
                dismissBottomSheet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatStatus(dobString: String, gender: String): String {
        val age = calculateAge(dobString) ?: return gender.lowercase().ifBlank { "" }
        val normalizedGender = gender.lowercase()
        return if (normalizedGender.isBlank()) {
            "$age year old"
        } else {
            "$age year old - $normalizedGender"
        }
    }

    private fun calculateAge(dobString: String): Int? {
        return runCatching {
            val birthDate = LocalDate.parse(dobString)
            val currentDate = LocalDate.now()
            Period.between(birthDate, currentDate).years
        }.getOrNull()
    }
}

internal fun resolveProfileUsername(
    username: String,
    displayName: String,
): String {
    val persistedUsername = username
        .trim()
        .removePrefix("@")
        .lowercase()
    if (persistedUsername.isNotBlank()) return persistedUsername

    return displayName
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .take(30)
        .ifBlank { "username" }
}
