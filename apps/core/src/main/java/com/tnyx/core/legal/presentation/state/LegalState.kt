package com.tnyx.core.legal.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class LegalSectionUiModel(
    val title: String,
    val body: String
)

@Immutable
data class LegalUiState(
    val title: String = "",
    val url: String = "",
    val isLoading: Boolean = false,
    val isRemoteEnabled: Boolean = false,
    val errorMessage: String? = null,
    val uiOnlyModeTitle: String = "UI-only mode is active",
    val uiOnlyModeSubtitle: String = "content will render from remote legal source once network-backed mode is enabled.",
    val enableRemoteDocsText: String = "Remote legal documents will replace this placeholder when backend-backed legal delivery is enabled.",
    val legalInfoText: String = "Your privacy and consent choices matter. Review this document carefully before proceeding.",
    val placeholderSections: List<LegalSectionUiModel> = DefaultLegalPlaceholderSections
)

sealed class LegalEffect {
    data object Close : LegalEffect()
}

internal val DefaultLegalPlaceholderSections = listOf(
    LegalSectionUiModel(
        title = "1. Data We Collect",
        body = "We may process account profile details, device metadata, language preference, and feature usage telemetry to improve the app experience and reliability."
    ),
    LegalSectionUiModel(
        title = "2. Why We Use It",
        body = "Data is used for core app operations, security checks, personalization, analytics, and issue diagnosis. We avoid unrelated processing in UI-only mode."
    ),
    LegalSectionUiModel(
        title = "3. Storage & Retention",
        body = "Data retention depends on account lifecycle and legal obligations. Temporary caches are removed automatically over time to maintain performance."
    ),
    LegalSectionUiModel(
        title = "4. Third-party Services",
        body = "When backend mode is enabled later, infrastructure vendors may be used for hosting, notifications, and authentication, following contractual safeguards."
    ),
    LegalSectionUiModel(
        title = "5. User Controls",
        body = "You can update preferences, request export, and request account deletion according to policy. Additional controls appear after backend integration."
    ),
    LegalSectionUiModel(
        title = "6. Security Measures",
        body = "We apply secure transport, least-privilege access, and monitoring practices. Sensitive operations are intended to run only on trusted backend paths."
    ),
    LegalSectionUiModel(
        title = "7. Policy Updates",
        body = "Legal text can evolve with product updates and regulatory changes. Material updates should be communicated in-app before new processing begins."
    ),
    LegalSectionUiModel(
        title = "8. Contact",
        body = "For policy-related concerns, support channels will be listed in production settings. This placeholder exists to validate modal scrolling behavior."
    )
)

