package com.tnyx.features.profile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileHomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                profileRepository.getProfile("demo-user")
                    .collect { profile ->
                        val age = calculateAge(profile.dob)
                        val formattedStatus = "$age year old - ${profile.gender.lowercase()}"
                        _uiState.update { state ->
                            state.copy(
                                displayName = profile.displayName,
                                status = formattedStatus,
                                planLabel = profile.planLabel,
                                weight = profile.weight,
                                height = profile.height,
                                bmi = profile.bmi,
                                bmr = profile.bmr
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateAge(dobString: String): Int {
        return try {
            val birthDate = java.time.LocalDate.parse(dobString)
            val currentDate = java.time.LocalDate.now()
            java.time.Period.between(birthDate, currentDate).years
        } catch (e: Exception) {
            35 // Default fallback age
        }
    }
}
