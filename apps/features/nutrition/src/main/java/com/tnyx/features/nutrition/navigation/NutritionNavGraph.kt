package com.tnyx.features.nutrition.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.nutrition.presentation.meal_diary.MealDiaryRoute
import com.tnyx.features.nutrition.presentation.meal_editor.MealEditorRoute
import com.tnyx.features.nutrition.presentation.meal_item_editor.MealItemEditorRoute
import com.tnyx.routing.routes.MainRoute

fun NavGraphBuilder.nutritionGraph(
    navController: androidx.navigation.NavHostController,
    onShowOverview: (String) -> Unit
) {
    navigation<MainRoute.NutritionGraph>(
        startDestination = NutritionScreen.MealDiary
    ) {
        composable<NutritionScreen.MealDiary> {
            MealDiaryRoute(
                onNavigateToMealDetail = { mealId ->
                    navController.navigate(NutritionScreen.MealEditor(mealId))
                },
                onNavigateToAddMeal = {
                    navController.navigate(NutritionScreen.MealEditor())
                },
                onShowOverview = onShowOverview
            )
        }

        composable<NutritionScreen.MealEditor> {
            MealEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemEditor = { itemId ->
                    navController.navigate(NutritionScreen.MealItemEditor(itemId))
                }
            )
        }

        composable<NutritionScreen.MealItemEditor> {
            MealItemEditorRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@kotlinx.serialization.Serializable
sealed interface NutritionScreen {
    @kotlinx.serialization.Serializable
    data object MealDiary : NutritionScreen

    @kotlinx.serialization.Serializable
    data class MealEditor(val mealId: String? = null) : NutritionScreen

    @kotlinx.serialization.Serializable
    data class MealItemEditor(val itemId: String) : NutritionScreen
}
