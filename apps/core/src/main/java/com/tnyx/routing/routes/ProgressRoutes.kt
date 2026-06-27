package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Public routes for Progress-owned user transformation surfaces.
 */
@Serializable
sealed interface ProgressRoute {
    @Serializable
    data object Home : ProgressRoute

    @Serializable
    data object Journey : ProgressRoute

    @Serializable
    data object ProgressPhotos : ProgressRoute

    @Serializable
    data object Measurements : ProgressRoute

    @Serializable
    data object Weight : ProgressRoute

    @Serializable
    data object Achievements : ProgressRoute

    @Serializable
    data object Analytics : ProgressRoute
}
