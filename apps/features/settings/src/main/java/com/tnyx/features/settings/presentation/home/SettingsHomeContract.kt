package com.tnyx.features.settings.presentation.home

data class SettingsHomeUiState(
    val displayName: String = "Arjun Verma",
    val email: String = "arjun.verma@gmail.com",
    val planLabel: String = "Pro Plan"
)

sealed interface SettingsHomeAction {
    data object BackClicked : SettingsHomeAction
    data object SearchClicked : SettingsHomeAction
    data object NotificationsClicked : SettingsHomeAction
    data object ProfileHeaderClicked : SettingsHomeAction
    
    // Account
    data object PersonalInfoClicked : SettingsHomeAction
    data object SubscriptionClicked : SettingsHomeAction
    
    // Preferences
    data object AppPreferencesClicked : SettingsHomeAction
    data object ManageNotificationsClicked : SettingsHomeAction
    data object LanguageClicked : SettingsHomeAction
    data object UnitsClicked : SettingsHomeAction
    data object ExportDataClicked : SettingsHomeAction
    
    // Support & About
    data object AboutClicked : SettingsHomeAction
    data object HelpFaqClicked : SettingsHomeAction
    data object ContactUsClicked : SettingsHomeAction
    
    // Auth
    data object LogoutClicked : SettingsHomeAction
}

sealed interface SettingsHomeEffect {
    data object NavigateBack : SettingsHomeEffect
    data object NavigateToPersonalInfo : SettingsHomeEffect
    data object NavigateToSubscription : SettingsHomeEffect
    data object NavigateToAppPreferences : SettingsHomeEffect
    data object NavigateToNotifications : SettingsHomeEffect
    data object NavigateToLanguage : SettingsHomeEffect
    data object NavigateToUnits : SettingsHomeEffect
    data object NavigateToExportData : SettingsHomeEffect
    data object NavigateToAbout : SettingsHomeEffect
    data object NavigateToHelpFaq : SettingsHomeEffect
    data object NavigateToContactUs : SettingsHomeEffect
    data object PerformLogout : SettingsHomeEffect
}
