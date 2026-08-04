package com.tnyx.wear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.tnyx.wear.presentation.history.WorkoutHistoryScreen
import com.tnyx.wear.presentation.home.HomeDashboardScreen
import com.tnyx.wear.presentation.nutrition.CalorieInputScreen
import com.tnyx.wear.presentation.nutrition.CalorieSummaryScreen
import com.tnyx.wear.presentation.nutrition.LogFoodScreen
import com.tnyx.wear.presentation.nutrition.NutritionSummaryScreen
import com.tnyx.wear.presentation.nutrition.WaterSelectionScreen
import com.tnyx.wear.presentation.settings.SettingsScreen
import com.tnyx.wear.presentation.settings.UnitsSettingsScreen
import com.tnyx.wear.presentation.workout.WorkoutListScreen
import com.tnyx.wear.theme.TnyxWearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TnyxWatchApp()
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun TnyxWatchApp() {
    val navController = rememberSwipeDismissableNavController()

    TnyxWearTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeDashboardScreen(
                        onNavigateToWorkout = { navController.navigate("workout") },
                        onNavigateToHistory = { navController.navigate("history") },
                        onNavigateToSummary = { navController.navigate("summary") },
                        onNavigateToNutrition = { navController.navigate("summary") },
                        onNavigateToAddFood = { navController.navigate("log_food") },
                        onNavigateToAddWater = { navController.navigate("add_water") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("summary") {
                    NutritionSummaryScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("calorie_summary") {
                    CalorieSummaryScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("add_water") {
                    WaterSelectionScreen(
                        onConfirm = { cups ->
                            Log.i("TnyxWatchApp", "Water logged: $cups cups")
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("log_food") {
                    LogFoodScreen(
                        onConfirm = { food ->
                            Log.i("TnyxWatchApp", "Food Logged: ${food.name} (${food.calories} kcal)")
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("calorie_input") {
                    CalorieInputScreen(
                        onConfirm = { calories ->
                            Log.i("TnyxWatchApp", "Calories Logged: $calories kcal")
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("workout") {
                    WorkoutListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onStartWorkout = { type ->
                            Log.i("TnyxWatchApp", "Starting workout session: $type")
                            // Will launch timer/tracking screen in next steps
                        }
                    )
                }
                composable("history") {
                    WorkoutHistoryScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToUnits = { navController.navigate("units_settings") },
                        onLogoutConfirmed = {
                            Log.i("TnyxWatchApp", "User Logged Out!")
                            navController.popBackStack("home", false)
                        }
                    )
                }
                composable("units_settings") {
                    UnitsSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
