package com.tnyx.features.settings.presentation.personal_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnyx.core.ui.components.inputs.Country
import com.tnyx.core.ui.components.inputs.cmToFeetInches
import com.tnyx.core.ui.components.inputs.countryForMobile
import com.tnyx.core.ui.components.inputs.feetInchesToCm
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.model.UserProfile
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionProvider: AuthSessionProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalInfoUiState())
    val uiState: StateFlow<PersonalInfoUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var activeProfile: UserProfile? = null

    init {
        val defaultCountry = countryForMobile("")
        update { it.copy(selectedCountry = defaultCountry) }
        loadProfileHeader()
    }

    private fun loadProfileHeader() {
        viewModelScope.launch {
            runCatching {
                profileRepository.getCurrentProfile().collect { profile ->
                    activeProfile = profile
                    update { state ->
                        if (state.hasChanges) {
                            state.copy(
                                avatarUrl = profile.avatarUrl,
                                membershipTier = profile.membershipTier,
                            )
                        } else {
                            state.copy(
                                fullName = profile.displayName,
                                username = profile.username,
                                email = sessionProvider.currentSession()?.email.orEmpty(),
                                avatarUrl = profile.avatarUrl,
                                membershipTier = profile.membershipTier,
                            )
                        }
                    }
                }
            }.onFailure {
                update { state ->
                    state.copy(avatarError = "Profile could not be loaded")
                }
            }
        }
    }

    fun init(onboardingState: Any?, authCurrentUser: Any?, authProfile: Any?) {
        // Reserved for the remaining account fields until typed contracts replace
        // the legacy placeholder inputs.
    }

    fun onAction(action: PersonalInfoAction) {
        when (action) {
            is PersonalInfoAction.OnFullNameChange -> update {
                it.copy(
                    fullName = action.name,
                    fullNameError = null,
                    saveError = null,
                    hasChanges = true,
                )
            }
            is PersonalInfoAction.OnUsernameChange -> update {
                it.copy(
                    username = action.username,
                    usernameError = null,
                    saveError = null,
                    hasChanges = true,
                )
            }
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
                update {
                    it.copy(
                        showDobPicker = false,
                        showHeightPopup = false,
                        showCountryPicker = false,
                        deleteStep = DeleteAccountStep.Idle,
                    )
                }
            }
            PersonalInfoAction.OnSaveClicked -> save()
            PersonalInfoAction.OnBackClicked -> Unit
            PersonalInfoAction.OnChangePhotoClicked -> Unit
            is PersonalInfoAction.OnAvatarBytesReady -> uploadAvatar(action.jpegBytes)
            PersonalInfoAction.OnAvatarProcessingFailed -> update {
                it.copy(avatarError = "This photo could not be processed. Choose another image.")
            }
            PersonalInfoAction.OnRemovePhotoClicked -> removeAvatar()
            PersonalInfoAction.OnDismissAvatarError -> update { it.copy(avatarError = null) }
            PersonalInfoAction.OnDeleteAccountClicked -> update { it.copy(deleteStep = DeleteAccountStep.Confirm) }
            PersonalInfoAction.OnKeepAccountClicked -> {
                countdownJob?.cancel()
                update { it.copy(deleteStep = DeleteAccountStep.Idle) }
            }
            PersonalInfoAction.OnConfirmDeleteClicked -> update {
                it.copy(
                    deleteStep = DeleteAccountStep.HoldToDelete,
                    remainingSeconds = 5,
                    holdProgress = 0f,
                )
            }
            PersonalInfoAction.OnHoldStarted -> startDeleteCountdown()
            PersonalInfoAction.OnHoldReleased -> stopDeleteCountdown()
            PersonalInfoAction.OnDeleteCompletedShown -> update { it.copy(deleteStep = DeleteAccountStep.Idle) }
        }
    }

    private fun uploadAvatar(jpegBytes: ByteArray) {
        if (jpegBytes.isEmpty() || _uiState.value.isAvatarUploading) return

        viewModelScope.launch {
            update {
                it.copy(
                    isAvatarUploading = true,
                    avatarError = null,
                )
            }

            runCatching {
                profileRepository.updateAvatar(jpegBytes)
            }.onSuccess { avatarUrl ->
                update {
                    it.copy(
                        avatarUrl = avatarUrl,
                        isAvatarUploading = false,
                    )
                }
            }.onFailure {
                update {
                    it.copy(
                        isAvatarUploading = false,
                        avatarError = "Profile photo could not be updated. Try again.",
                    )
                }
            }
        }
    }

    private fun removeAvatar() {
        if (_uiState.value.isAvatarUploading || _uiState.value.avatarUrl.isNullOrBlank()) return

        viewModelScope.launch {
            update {
                it.copy(
                    isAvatarUploading = true,
                    avatarError = null,
                )
            }

            runCatching {
                profileRepository.removeAvatar()
            }.onSuccess {
                update {
                    it.copy(
                        avatarUrl = null,
                        isAvatarUploading = false,
                    )
                }
            }.onFailure {
                update {
                    it.copy(
                        isAvatarUploading = false,
                        avatarError = "Profile photo could not be removed. Try again.",
                    )
                }
            }
        }
    }

    private fun toggleHeightUnit(newUnit: String) {
        update { state ->
            if (state.heightUnit == newUnit) return@update state

            if (newUnit == "ft") {
                val cm = state.heightCm.toFloatOrNull() ?: 0f
                val (ft, inch) = cmToFeetInches(cm)
                state.copy(
                    heightUnit = newUnit,
                    heightFeet = ft.toString(),
                    heightInches = inch.toString(),
                    hasChanges = true,
                )
            } else {
                val cm = feetInchesToCm(state.heightFeet, state.heightInches)
                val cmText = cm?.let {
                    if (it % 1f == 0f) it.toInt().toString() else it.toString()
                }.orEmpty()
                state.copy(
                    heightUnit = newUnit,
                    heightCm = cmText,
                    hasChanges = true,
                )
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
                        remainingSeconds = remaining.coerceAtLeast(0),
                    )
                }
            }

            update {
                it.copy(
                    deleteStep = DeleteAccountStep.Completed,
                    remainingSeconds = 0,
                    holdProgress = 1f,
                )
            }
        }
    }

    private fun stopDeleteCountdown() {
        countdownJob?.cancel()
        update {
            it.copy(
                remainingSeconds = 5,
                holdProgress = 0f,
            )
        }
    }

    private fun save() {
        val state = _uiState.value
        val normalizedUsername = state.username
            .trim()
            .removePrefix("@")
            .lowercase()
        val fullNameError = if (state.fullName.isBlank()) "Full name is required" else null
        val usernameError = if (!USERNAME_PATTERN.matches(normalizedUsername)) {
            "Use 3-30 lowercase letters, numbers, or underscores"
        } else {
            null
        }

        if (fullNameError != null || usernameError != null) {
            update {
                it.copy(
                    fullNameError = fullNameError,
                    usernameError = usernameError,
                    saveError = null,
                )
            }
            return
        }

        val profile = activeProfile
        if (profile == null) {
            update { it.copy(saveError = "Profile is not ready. Try again.") }
            return
        }

        val updatedProfile = profile.copy(
            displayName = state.fullName.trim(),
            username = normalizedUsername,
        )

        viewModelScope.launch {
            update {
                it.copy(
                    isSaving = true,
                    fullNameError = null,
                    usernameError = null,
                    saveError = null,
                )
            }
            runCatching {
                profileRepository.updateProfile(updatedProfile)
            }.onSuccess {
                activeProfile = updatedProfile
                update {
                    it.copy(
                        fullName = updatedProfile.displayName,
                        username = updatedProfile.username,
                        isSaving = false,
                        hasChanges = false,
                    )
                }
            }.onFailure {
                update {
                    it.copy(
                        isSaving = false,
                        saveError = "Profile could not be saved. Try again.",
                    )
                }
            }
        }
    }

    private inline fun update(block: (PersonalInfoUiState) -> PersonalInfoUiState) {
        _uiState.update(block)
    }

    private companion object {
        val USERNAME_PATTERN = Regex("[a-z0-9_]{3,30}")
    }
}
