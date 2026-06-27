package com.tnyx.features.progress.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.progress.presentation.home.ProgressHomeRoute
import com.tnyx.routing.routes.MainRoute
import com.tnyx.routing.routes.ProgressRoute

fun NavGraphBuilder.progressGraph(
    navController: NavHostController
) {
    navigation<MainRoute.ProgressGraph>(
        startDestination = ProgressRoute.Home
    ) {
        composable<ProgressRoute.Home> {
            ProgressHomeRoute()
        }

        composable<ProgressRoute.Journey> {
            ProgressPlaceholderScreen("Journey")
        }

        composable<ProgressRoute.ProgressPhotos> {
            ProgressPlaceholderScreen("Progress Photos")
        }

        composable<ProgressRoute.Measurements> {
            ProgressPlaceholderScreen("Measurements")
        }

        composable<ProgressRoute.Weight> {
            ProgressPlaceholderScreen("Weight")
        }

        composable<ProgressRoute.Achievements> {
            ProgressPlaceholderScreen("Achievements")
        }

        composable<ProgressRoute.Analytics> {
            ProgressPlaceholderScreen("Progress Analytics")
        }
    }
}

@Composable
private fun ProgressPlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
