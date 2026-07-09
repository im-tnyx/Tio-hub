package com.tnyx.features.settings.presentation.personal_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.core.ui.components.inputs.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInfoViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalInfoUiState())
    val uiState: StateFlow<PersonalInfoUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        val defaultCountry = countryForMobile("")
        update { it.copy(selectedCountry = defaultCountry) }
    }

    /**
     * Initialize state from external view models (provided by user snippet)
     * Note: Types like OnboardingViewModel/AuthViewModel should be imported if available.
     */
    fun init(onboardingState: Any?, authCurrentUser: Any?, authProfile: Any?) {
        // This is a placeholder adaptation of the user's provided logic
        // because the actual ViewModel classes might reside in different modules/packages.
        // In a real scenario, you would pass the specific State or Data classes.
    }

    fun onAction(action: PersonalInfoAction) {
        when (action) {
            is PersonalInfoAction.OnFullNameChange -> update { it.copy(fullName = action.name, hasChanges = true) }
            is PersonalInfoAction.OnEmailChange -> update { it.copy(email = action.email, hasChanges = true) }
            is PersonalInfoAction.OnMobileChange -> update { it.copy(phoneNumber = action.value, hasChanges = true) }
            is PersonalInfoAction.OnCountrySelected -> update { it.copy(selectedCountry = action.country, hasChanges = true) }
            PersonalInfoAction.OnCountryPickerClicked -> update { it.copy(showCountryPicker = true) }
            is PersonalInfoAction.OnGenderChange -> update { it.copy(gender = action.gender, hasChanges = true) }
            is PersonalInfoAction.OnDobChange -> update { it.copy(dobMillis = action.millis, hasChanges = true) }
            is PersonalInfoAction.OnHeightUnitChange -> toggleHeightUnit(action.unit)
            is PersonalInfoAction.OnHeightCmChange -> update { it.copy(heightCm = action.value, hasChanges = true) }
            is PersonalInfoAction.OnHeightFeetChange -> update { it.copy(heightFeet = action.value, hasChanges = true) }
            is PersonalInfoAction.OnHeightInchesChange -> update { it.copy(heightInches = action.value, hasChanges = true) }
            PersonalInfoAction.OnHeightEditClicked -> update { it.copy(showHeightPopup = true) }
            PersonalInfoAction.OnDobClicked -> update { it.copy(showDobPicker = true) }
            PersonalInfoAction.OnDismissOverlays -> {
                countdownJob?.cancel()
                update { it.copy(showDobPicker = false, showHeightPopup = false, showCountryPicker = false, deleteStep = DeleteAccountStep.Idle) }
            }
            PersonalInfoAction.OnSaveClicked -> save()
            PersonalInfoAction.OnBackClicked -> { /* Handled by route */ }
            PersonalInfoAction.OnChangePhotoClicked -> { /* TODO */ }
            
            // Delete Account Flow
            PersonalInfoAction.OnDeleteAccountClicked -> update { it.copy(deleteStep = DeleteAccountStep.Confirm) }
            PersonalInfoAction.OnKeepAccountClicked -> {
                countdownJob?.cancel()
                update { it.copy(deleteStep = DeleteAccountStep.Idle) }
            }
            PersonalInfoAction.OnConfirmDeleteClicked -> update { it.copy(deleteStep = DeleteAccountStep.HoldToDelete, remainingSeconds = 5, holdProgress = 0f) }
            PersonalInfoAction.OnHoldStarted -> startDeleteCountdown()
            PersonalInfoAction.OnHoldReleased -> stopDeleteCountdown()
            PersonalInfoAction.OnDeleteCompletedShown -> update { it.copy(deleteStep = DeleteAccountStep.Idle) }
        }
    }

    private fun toggleHeightUnit(newUnit: String) {
        update { state ->
            if (state.heightUnit == newUnit) return@update state
            
            if (newUnit == "ft") {
                val cm = state.heightCm.toFloatOrNull() ?: 0f
                val (ft, inch) = cmToFeetInches(cm)
                state.copy(heightUnit = newUnit, heightFeet = ft.toString(), heightInches = inch.toString(), hasChanges = true)
            } else {
                val cm = feetInchesToCm(state.heightFeet, state.heightInches)
                val cmText = cm?.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }.orEmpty()
                state.copy(heightUnit = newUnit, heightCm = cmText, hasChanges = true)
            }
        }
    }

    private fun startDeleteCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val totalMillis = 5000L
            val tickMillis = 50L
            var currentMillis = 0L

            while (currentMillis < totalMillis) {
                delay(tickMillis)
                currentMillis += tickMillis

                val progress = currentMillis.toFloat() / totalMillis
                val remaining = ((totalMillis - currentMillis) / 1000).toInt() + 1

                update {
                    it.copy(
                        holdProgress = progress,
                        remainingSeconds = remaining.coerceAtLeast(0)
                    )
                }
            }

            update {
                it.copy(
                    deleteStep = DeleteAccountStep.Completed,
                    remainingSeconds = 0,
                    holdProgress = 1f
                )
            }
        }
    }

    private fun stopDeleteCountdown() {
        countdownJob?.cancel()
        update {
            it.copy(
                remainingSeconds = 5,
                holdProgress = 0f
            )
        }
    }

    private fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            delay(1000) // Simulate
            _uiState.update { it.copy(isSaving = false, hasChanges = false) }
        }
    }

    private inline fun update(block: (PersonalInfoUiState) -> PersonalInfoUiState) {
        _uiState.update(block)
    }
}
