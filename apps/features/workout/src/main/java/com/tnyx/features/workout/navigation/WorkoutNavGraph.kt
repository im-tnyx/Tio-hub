package com.tnyx.features.workout.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.routing.routes.MainRoute

/**
 * Workout feature NavGraph.
 * Owns the 3 secondary tab destinations: History, Explore, Routines.
 * Secondary nav bar (WorkoutSecondaryNav) navigates between these destinations.
 */
fun NavGraphBuilder.workoutGraph(
    navController: NavHostController
) {
    navigation<MainRoute.WorkoutGraph>(
        startDestination = WorkoutScreen.History
    ) {
        composable<WorkoutScreen.History> {
            // TODO: Replace with real WorkoutHistoryRoute
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Workout History")
            }
        }

        composable<WorkoutScreen.Explore> {
            // TODO: Replace with real WorkoutExploreRoute
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Explore Workouts")
            }
        }

        composable<WorkoutScreen.Routines> {
            // TODO: Replace with real WorkoutRoutinesRoute
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("My Routines")
            }
        }
    }
}

/**
 * Type-safe route definitions for Workout sub-sections.
 */
@kotlinx.serialization.Serializable
sealed interface WorkoutScreen {
    @kotlinx.serialization.Serializable
    data object History : WorkoutScreen

    @kotlinx.serialization.Serializable
    data object Explore : WorkoutScreen

    @kotlinx.serialization.Serializable
    data object Routines : WorkoutScreen
}
