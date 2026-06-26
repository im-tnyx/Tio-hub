package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Main routes for the BottomNav shell.
 */
@Serializable
sealed interface MainRoute {
    @Serializable
    data object Home : MainRoute

    @Serializable
    data object NutritionGraph : MainRoute

    @Serializable
    data object AiCoach : MainRoute

    @Serializable
    data object WorkoutGraph : MainRoute

    @Serializable
    data object Profile : MainRoute
}
