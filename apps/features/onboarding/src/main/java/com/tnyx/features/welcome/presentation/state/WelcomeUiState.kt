package com.tnyx.features.welcome.presentation.state

enum class LegalDocumentType {
    TERMS_AND_CONDITIONS,
    PRIVACY_POLICY
}

data class WelcomeUiState(
    val localeCode: String = "en",
    val skipText: String = "Skip",
    val title: String = "Train Smarter. Eat Better.\nStay Consistent.",
    val featureLines: List<String> = listOf(
        "Log meals your way",
        "Follow personalized workout plans",
        "Get AI insights and progress summaries"
    ),
    val ctaText: String = "Get Started",
    val signInText: String = "Sign In",
    val termsPrefix: String = "By continuing, you agree to our ",
    val termsText: String = "Terms & Conditions",
    val andText: String = " and ",
    val privacyText: String = "Privacy Policy",
    val termsSuffix: String = ".",
    
    // UI states for overlays
    val showLanguageSheet: Boolean = false
)

sealed class WelcomeEffect {
    data object NavigateToHome : WelcomeEffect()
    data object NavigateToLogin : WelcomeEffect()
    data object ShowLanguageSelector : WelcomeEffect()
    data class NavigateToLegal(val title: String, val url: String) : WelcomeEffect()
}
