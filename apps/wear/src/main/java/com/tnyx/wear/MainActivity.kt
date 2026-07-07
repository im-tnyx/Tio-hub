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
import com.tnyx.wear.presentation.settings.SettingsScreen
import com.tnyx.wear.presentation.workout.WorkoutListScreen
import com.tnyx.wear.theme.SamsungHealthWearTheme
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

    SamsungHealthWearTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeDashboardScreen(
                        onNavigateToWorkout = { navController.navigate("workout") },
                        onNavigateToHistory = { navController.navigate("history") },
                        onNavigateToSummary = { Log.i("TnyxWatchApp", "Navigate to Summary") },
                        onNavigateToNutrition = { Log.i("TnyxWatchApp", "Navigate to Nutrition") },
                        onNavigateToAddFood = { Log.i("TnyxWatchApp", "Navigate to Add Food") },
                        onNavigateToAddWater = { Log.i("TnyxWatchApp", "Navigate to Add Water") },
                        onNavigateToSettings = { navController.navigate("settings") }
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
                        onLogoutConfirmed = {
                            Log.i("TnyxWatchApp", "User Logged Out!")
                            navController.popBackStack("home", false)
                        }
                    )
                }
            }
        }
    }
}
