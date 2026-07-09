package com.tnyx.features.settings.presentation.personal_info

import com.tnyx.core.ui.components.inputs.Country

enum class DeleteAccountStep {
    Idle,
    Confirm,
    HoldToDelete,
    Completed
}

data class PersonalInfoUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val selectedCountry: Country? = null,
    val gender: String = "", // "Male" | "Female" | "Other"
    val dobMillis: Long = 0L,
    val heightUnit: String = "cm", // "cm" | "ft"
    val heightCm: String = "",
    val heightFeet: String = "5",
    val heightInches: String = "7",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    
    // Overlays
    val showDobPicker: Boolean = false,
    val showHeightPopup: Boolean = false,
    val showCountryPicker: Boolean = false,
    
    // Delete Account Flow
    val deleteStep: DeleteAccountStep = DeleteAccountStep.Idle,
    val holdProgress: Float = 0f,
    val remainingSeconds: Int = 5
)

sealed interface PersonalInfoAction {
    data object OnBackClicked : PersonalInfoAction
    data object OnSaveClicked : PersonalInfoAction
    data object OnChangePhotoClicked : PersonalInfoAction
    data class OnFullNameChange(val name: String) : PersonalInfoAction
    data class OnEmailChange(val email: String) : PersonalInfoAction
    data class OnMobileChange(val value: String) : PersonalInfoAction
    data class OnCountrySelected(val country: Country) : PersonalInfoAction
    data object OnCountryPickerClicked : PersonalInfoAction
    data class OnGenderChange(val gender: String) : PersonalInfoAction
    data object OnDobClicked : PersonalInfoAction
    data class OnDobChange(val millis: Long) : PersonalInfoAction
    data class OnHeightUnitChange(val unit: String) : PersonalInfoAction
    data class OnHeightCmChange(val value: String) : PersonalInfoAction
    data class OnHeightFeetChange(val value: String) : PersonalInfoAction
    data class OnHeightInchesChange(val value: String) : PersonalInfoAction
    data object OnHeightEditClicked : PersonalInfoAction
    data object OnDismissOverlays : PersonalInfoAction
    
    // Delete Account Actions
    data object OnDeleteAccountClicked : PersonalInfoAction
    data object OnKeepAccountClicked : PersonalInfoAction
    data object OnConfirmDeleteClicked : PersonalInfoAction
    data object OnHoldStarted : PersonalInfoAction
    data object OnHoldReleased : PersonalInfoAction
    data object OnDeleteCompletedShown : PersonalInfoAction
}
