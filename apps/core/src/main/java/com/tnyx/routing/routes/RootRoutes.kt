package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Top-level routes for the entire application.
 * These usually live in the Root NavHost.
 */
@Serializable
sealed interface RootRoute {
    @Serializable
    data object Splash : RootRoute

    @Serializable
    data object Welcome : RootRoute

    @Serializable
    data class Onboarding(
        val entryPath: String = "GetStarted",
        val authState: String = "SignedOut",
        val signupCompleted: Boolean = false,
        val workoutPlanEnabled: Boolean? = null,
        val mobilePresent: Boolean = false,
        val mobileVerified: Boolean = false,
        val namePrefilled: Boolean = false,
        val authRequired: Boolean = false,
    ) : RootRoute

    @Serializable
    data object AuthGraph : RootRoute

    @Serializable
    data object MainGraph : RootRoute

    @Serializable
    data class Legal(
        val title: String,
        val url: String,
        val isRemoteEnabled: Boolean = false
    ) : RootRoute
}
