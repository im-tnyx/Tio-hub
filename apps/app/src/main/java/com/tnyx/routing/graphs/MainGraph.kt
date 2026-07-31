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
import com.tnyx.core.ui.shell.domain.model.ShellTab
import com.tnyx.core.ui.shell.domain.model.deriveHomeExperienceMode
import com.tnyx.features.home.presentation.home.HomeRoute
import com.tnyx.features.nutrition.navigation.nutritionGraph
import com.tnyx.features.profile.presentation.home.ProfileHomeRoute
import com.tnyx.features.progress.navigation.progressGraph
import com.tnyx.features.workout.navigation.WorkoutDestination
import com.tnyx.features.workout.navigation.workoutGraph
import com.tnyx.features.workout.presentation.library.ExerciseLibraryRoute
import com.tnyx.routing.routes.MainRoute

/**
 * Nested Graph for Main Shell (Bottom Navigation).
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    enabledTabs: List<ShellTab>,
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
) {
    composable<MainRoute.Home> {
        HomeRoute(
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
        ExerciseLibraryRoute(
            onNavigateBack = { navController.popBackStack() },
            onSearchClick = {
                navController.navigate(WorkoutDestination.SearchExercises)
            },
            onCreateProgramClick = {},
            onCreateRoutineClick = {},
            onCreateExerciseClick = {
                navController.navigate(WorkoutDestination.CreateExercise)
            }
        )
    }

    progressGraph(navController = navController)

    composable<MainRoute.You> {
        ProfileHomeRoute(
            onOpenSettings = onOpenSettings,
            onOpenEditProfile = onOpenPersonalInfo,
            onNavigateBack = {
                // You is a top-level tab. Its header intentionally has no Back action.
            },
            showBackButton = false,
        )
    }
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
