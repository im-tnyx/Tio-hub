package com.tnyx.features.onboarding.domain.analytics

class OnboardingAnalyticsLogger {
    fun log(
        eventName: String,
        params: Map<String, String> = emptyMap(),
    ) {
        // Placeholder logger keeps the event contract stable until a real analytics sink is wired.
        eventName.length + params.size
    }
}
