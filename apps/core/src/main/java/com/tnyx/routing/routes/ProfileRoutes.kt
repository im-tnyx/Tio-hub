package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Public routes for the avatar-launched Profile graph.
 *
 * Profile is a Fitness Hub + Account Launcher, not a business domain owner.
 */
@Serializable
sealed interface ProfileRoute {
    @Serializable
    data object Graph : ProfileRoute

    @Serializable
    data object Home : ProfileRoute

    @Serializable
    data object PersonalInfo : ProfileRoute
}
