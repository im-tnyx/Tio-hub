package com.tnyx.features.settings.presentation.personal_info

data class PersonalInfoUiState(
    val name: String = "",
    val email: String = "",
    val gender: String = "", // "Male" | "Female" | "Other" (UI-only)
    val dob: String = "",    // YYYY-MM-DD (UI-only; date picker not wired)
    val heightCm: String = "",
    val weightKg: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false
)

sealed interface PersonalInfoAction {
    data object BackClicked : PersonalInfoAction
    data object SaveClicked : PersonalInfoAction
    data object ChangePhotoClicked : PersonalInfoAction
    data class NameChanged(val value: String) : PersonalInfoAction
    data class EmailChanged(val value: String) : PersonalInfoAction
    data class GenderChanged(val value: String) : PersonalInfoAction
    data object DobClicked : PersonalInfoAction
    data class DobChanged(val value: String) : PersonalInfoAction
    data class HeightChanged(val value: String) : PersonalInfoAction
    data class WeightChanged(val value: String) : PersonalInfoAction
}
