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
