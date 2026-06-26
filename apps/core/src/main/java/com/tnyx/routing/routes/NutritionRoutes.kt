package com.tnyx.routing.routes

import kotlinx.serialization.Serializable

/**
 * Routes for the Nutrition feature.
 */
@Serializable
sealed interface NutritionRoute {
    @Serializable
    data object Home : NutritionRoute

    @Serializable
    data class MealDetails(val mealId: String) : NutritionRoute

    @Serializable
    data object AddFood : NutritionRoute
}
