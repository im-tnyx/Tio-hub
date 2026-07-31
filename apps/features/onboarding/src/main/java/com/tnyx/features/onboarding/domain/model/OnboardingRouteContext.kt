package com.tnyx.features.onboarding.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class OnboardingEntryPath {
    GetStarted,
    Skip,
    SignIn,
}

@Serializable
enum class OnboardingAuthState {
    SignedOut,
    SignedIn,
}

@Serializable
data class OnboardingRouteContext(
    val entryPath: OnboardingEntryPath = OnboardingEntryPath.GetStarted,
    val authState: OnboardingAuthState = OnboardingAuthState.SignedOut,
    val signupCompleted: Boolean = false,
    val workoutPlanEnabled: Boolean? = null,
    val mobilePresent: Boolean = false,
    val mobileVerified: Boolean = false,
    val namePrefilled: Boolean = false,
    val authRequired: Boolean = false,
)
