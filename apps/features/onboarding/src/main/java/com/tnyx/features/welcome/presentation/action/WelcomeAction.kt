package com.tnyx.features.welcome.presentation.action

import com.tnyx.features.welcome.presentation.state.LegalDocumentType

sealed class WelcomeAction {
    data object GetStartedClicked : WelcomeAction()
    data object SignInClicked : WelcomeAction()
    data object SkipForNowClicked : WelcomeAction()
    data object LanguageSelectorClicked : WelcomeAction()
    data object LanguageSheetDismissed : WelcomeAction()
    data class LanguageChanged(val localeCode: String) : WelcomeAction()
    data class LegalDocumentClicked(val type: LegalDocumentType) : WelcomeAction()
}
