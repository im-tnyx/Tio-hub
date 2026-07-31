package com.tnyx.features.onboarding.domain.analytics

sealed interface OnboardingAnalyticsEvent {
    data class ScreenView(
        val sectionId: String,
        val stepId: String,
    ) : OnboardingAnalyticsEvent

    data class NextClicked(
        val sectionId: String,
    ) : OnboardingAnalyticsEvent

    data class BackClicked(
        val sectionId: String,
    ) : OnboardingAnalyticsEvent

    data object OnboardingCompleted : OnboardingAnalyticsEvent

    data class UserAction(
        val sectionId: String,
        val actionName: String,
    ) : OnboardingAnalyticsEvent
}
