package com.tnyx.routing.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.nutrition.navigation.nutritionGraph
import com.tnyx.features.progress.navigation.progressGraph
import com.tnyx.features.workout.navigation.workoutGraph
import com.tnyx.routing.routes.MainRoute

/**
 * Nested Graph for Main Shell (Bottom Navigation).
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {
    composable<MainRoute.Home> {
        PlaceholderScreen("Home Screen")
    }

    nutritionGraph(
        navController = navController,
        onShowOverview = { /* Handle */ }
    )

    composable<MainRoute.AiCoach> {
        PlaceholderScreen("AI Coach Screen")
    }

    workoutGraph(navController = navController)

    progressGraph(navController = navController)
}

@androidx.compose.runtime.Composable
private fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}

