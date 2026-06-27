package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Public routes for the gear-launched Settings graph.
 */
@Serializable
sealed interface SettingsRoute {
    @Serializable
    data object Graph : SettingsRoute

    @Serializable
    data object Home : SettingsRoute

    @Serializable
    data object AppPreferences : SettingsRoute

    @Serializable
    data object Notifications : SettingsRoute

    @Serializable
    data object Units : SettingsRoute

    @Serializable
    data object Account : SettingsRoute

    @Serializable
    data object ExportData : SettingsRoute

    @Serializable
    data object About : SettingsRoute
}
