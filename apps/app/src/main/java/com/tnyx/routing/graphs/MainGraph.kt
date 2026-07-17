package com.tnyx.routing.graphs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tnyx.core.ui.shell.domain.model.HomeExperienceMode
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.model.deriveHomeExperienceMode
import com.tnyx.features.nutrition.navigation.nutritionGraph
import com.tnyx.features.profile.presentation.home.ProfileHomeRoute
import com.tnyx.features.progress.navigation.progressGraph
import com.tnyx.features.workout.navigation.workoutGraph
import com.tnyx.routing.routes.MainRoute

/**
 * Nested Graph for Main Shell (Bottom Navigation).
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    enabledTabs: List<ShellTab>,
    onOpenSettings: () -> Unit,
) {
    composable<MainRoute.Home> {
        AdaptiveHomeFoundation(
            mode = deriveHomeExperienceMode(enabledTabs),
        )
    }

    nutritionGraph(
        navController = navController,
        onShowOverview = { /* Handle */ },
    )

    composable<MainRoute.MealPlan> {
        TopLevelFoundationScreen(
            title = "Meal Plan",
            description = "Daily and weekly plans, meal suggestions and future grocery planning live here.",
        )
    }

    composable<MainRoute.AiCoach> {
        TopLevelFoundationScreen(
            title = "Tio",
            description = "Personal coaching and cross-domain suggestions live here.",
        )
    }

    workoutGraph(navController = navController)

    composable<MainRoute.WorkoutLibrary> {
        TopLevelFoundationScreen(
            title = "Library",
            description = "Exercises, saved routines, programs and templates live here.",
        )
    }

    progressGraph(navController = navController)

    composable<MainRoute.You> {
        ProfileHomeRoute(
            onOpenSettings = onOpenSettings,
            onOpenProgress = {
                if (ShellTab.Progress in enabledTabs) {
                    navController.navigate(MainRoute.ProgressGraph)
                } else {
                    navController.navigate(MainRoute.Home)
                }
            },
            onNavigateBack = {
                navController.navigate(MainRoute.Home) {
                    launchSingleTop = true
                }
            },
        )
    }
}

@Composable
private fun AdaptiveHomeFoundation(mode: HomeExperienceMode) {
    val focus = when (mode) {
        HomeExperienceMode.Nutrition -> "Nutrition-focused summary"
        HomeExperienceMode.Workout -> "Workout-focused summary"
        HomeExperienceMode.Balanced -> "Balanced coaching summary"
        HomeExperienceMode.Custom -> "Custom summary"
    }

    TopLevelFoundationScreen(
        title = "Home",
        description = "$focus. Detailed actions remain inside their owning tabs.",
    )
}

@Composable
private fun TopLevelFoundationScreen(
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
            Text(text = description)
        }
    }
}
