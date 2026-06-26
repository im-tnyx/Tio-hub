package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Routes for the Authentication flow.
 */
@Serializable
sealed interface AuthRoute {
    @Serializable
    data object Login : AuthRoute

    @Serializable
    data object Signup : AuthRoute

    @Serializable
    data class OtpVerification(val email: String) : AuthRoute
}
