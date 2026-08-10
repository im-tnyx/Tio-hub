package com.tnyx.features.nutrition.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tnyx.features.nutrition.presentation.meal_camera.MealCameraRoute
import com.tnyx.features.nutrition.presentation.meal_diary.MealDiaryRoute
import com.tnyx.features.nutrition.presentation.meal_editor.MealEditorRoute
import com.tnyx.features.nutrition.presentation.meal_item_editor.MealItemEditorRoute
import com.tnyx.features.nutrition.presentation.search.MealSearchRoute
import com.tnyx.features.nutrition.presentation.targets.NutritionTargetsRoute
import com.tnyx.routing.routes.MainRoute
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val MEAL_CHANGED_RESULT_KEY = "nutrition_meal_changed"
private const val MEAL_ITEM_RESULT_KEY = "nutrition_meal_item_result"
private const val MEAL_ITEM_REMOVED_RESULT_KEY = "nutrition_meal_item_removed"

fun NavGraphBuilder.nutritionGraph(
    navController: androidx.navigation.NavHostController,
    onShowOverview: (String) -> Unit
) {
    navigation<MainRoute.NutritionGraph>(
        startDestination = NutritionScreen.MealDiary
    ) {
        composable<NutritionScreen.MealDiary> { backStackEntry ->
            val mealChanged by backStackEntry.savedStateHandle
                .getStateFlow(MEAL_CHANGED_RESULT_KEY, false)
                .collectAsState()
            MealDiaryRoute(
                onNavigateToMealDetail = { mealId ->
                    navController.navigate(NutritionScreen.MealEditor(mealId = mealId))
                },
                onNavigateToSearch = { date ->
                    navController.navigate(NutritionScreen.MealSearch(date = date.toString()))
                },
                onNavigateToMealCamera = { date ->
                    navController.navigate(NutritionScreen.MealCamera(date = date.toString()))
                },
                onShowOverview = { target ->
                    navController.navigate(NutritionScreen.Overview(target))
                },
                onNavigateToNutritionSettings = {
                    navController.navigate(NutritionScreen.Targets)
                },
                refreshSignal = mealChanged,
                onRefreshConsumed = {
                    backStackEntry.savedStateHandle[MEAL_CHANGED_RESULT_KEY] = false
                },
            )
        }

        composable<NutritionScreen.Overview> {
            com.tnyx.features.nutrition.presentation.nutrition_overview.NutritionOverviewRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NutritionScreen.MealSearch> { backStackEntry ->
            val route = backStackEntry.toRoute<NutritionScreen.MealSearch>()
            MealSearchRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMealEditor = { date, initialItem ->
                    if (route.returnItemResult && initialItem != null) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(MEAL_ITEM_RESULT_KEY, Json.encodeToString(initialItem))
                        navController.popBackStack()
                    } else {
                        navController.navigate(
                            NutritionScreen.MealEditor(
                                date = date.toString(),
                                initialItemJson = initialItem?.let(Json::encodeToString),
                            ),
                        )
                    }
                }
            )
        }

        composable<NutritionScreen.MealCamera> { backStackEntry ->
            val route = backStackEntry.toRoute<NutritionScreen.MealCamera>()
            MealCameraRoute(
                onNavigateBack = { navController.popBackStack() },
                onBarcodeScanned = { barcode ->
                    navController.navigate(
                        NutritionScreen.MealSearch(
                            date = route.date,
                            initialQuery = barcode,
                        ),
                    )
                },
                onBarcodeResolved = { item ->
                    navController.navigate(
                        NutritionScreen.MealEditor(
                            date = route.date,
                            initialItemJson = Json.encodeToString(item),
                        ),
                    ) {
                        popUpTo<NutritionScreen.MealCamera> { inclusive = true }
                    }
                },
                onOpenMealEditor = { meal, photoPath, photoMimeType ->
                    navController.navigate(
                        NutritionScreen.MealEditor(
                            date = route.date,
                            initialMealJson = Json.encodeToString(meal),
                            initialPhotoPath = photoPath,
                            initialPhotoMimeType = photoMimeType,
                        )
                    ) {
                        popUpTo<NutritionScreen.MealCamera> { inclusive = true }
                    }
                },
            )
        }

        composable<NutritionScreen.MealEditor> { backStackEntry ->
            val route = backStackEntry.toRoute<NutritionScreen.MealEditor>()
            val itemResult by backStackEntry.savedStateHandle
                .getStateFlow<String?>(MEAL_ITEM_RESULT_KEY, null)
                .collectAsState()
            val removedItemId by backStackEntry.savedStateHandle
                .getStateFlow<String?>(MEAL_ITEM_REMOVED_RESULT_KEY, null)
                .collectAsState()
            MealEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onMealChanged = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(MEAL_CHANGED_RESULT_KEY, true)
                    navController.popBackStack()
                },
                onNavigateToSearch = { date ->
                    navController.navigate(
                        NutritionScreen.MealSearch(
                            date = date.toString(),
                            returnItemResult = true,
                        ),
                    )
                },
                onNavigateToItemEditor = { item ->
                    navController.navigate(
                        NutritionScreen.MealItemEditor(
                            itemJson = item?.let(Json::encodeToString),
                        ),
                    )
                },
                itemResult = itemResult,
                removedItemId = removedItemId,
                onItemResultConsumed = {
                    backStackEntry.savedStateHandle[MEAL_ITEM_RESULT_KEY] = null
                },
                onItemRemovalConsumed = {
                    backStackEntry.savedStateHandle[MEAL_ITEM_REMOVED_RESULT_KEY] = null
                },
                initialPhotoPath = route.initialPhotoPath,
                initialPhotoMimeType = route.initialPhotoMimeType,
            )
        }

        composable<NutritionScreen.MealItemEditor> {
            MealItemEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onItemSaved = { item ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(MEAL_ITEM_RESULT_KEY, Json.encodeToString(item))
                    navController.popBackStack()
                },
                onItemRemoved = { itemId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(MEAL_ITEM_REMOVED_RESULT_KEY, itemId)
                    navController.popBackStack()
                },
            )
        }

        composable<NutritionScreen.Targets> {
            NutritionTargetsRoute(
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
    data class MealSearch(
        val date: String? = null,
        val returnItemResult: Boolean = false,
        val initialQuery: String? = null,
    ) : NutritionScreen

    @kotlinx.serialization.Serializable
    data class MealCamera(val date: String? = null) : NutritionScreen

    @kotlinx.serialization.Serializable
    data class MealEditor(
        val mealId: String? = null,
        val date: String? = null,
        val initialItemJson: String? = null,
        val initialMealJson: String? = null,
        val initialPhotoPath: String? = null,
        val initialPhotoMimeType: String? = null,
    ) : NutritionScreen

    @kotlinx.serialization.Serializable
    data class MealItemEditor(val itemJson: String? = null) : NutritionScreen

    @kotlinx.serialization.Serializable
    data object Targets : NutritionScreen

    @kotlinx.serialization.Serializable
    data class Overview(val target: String? = "all") : NutritionScreen
}
