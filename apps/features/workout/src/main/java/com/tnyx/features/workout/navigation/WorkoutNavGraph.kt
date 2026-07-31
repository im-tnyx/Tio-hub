package com.tnyx.features.workout.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.workout.presentation.WorkoutHistoryRoute
import com.tnyx.features.workout.presentation.WorkoutRoute
import com.tnyx.features.workout.presentation.library.ExerciseLibraryRoute
import com.tnyx.routing.routes.MainRoute
import kotlinx.serialization.Serializable

fun NavGraphBuilder.workoutGraph(
    navController: NavHostController,
) {
    navigation<MainRoute.WorkoutGraph>(
        startDestination = WorkoutDestination.Home,
    ) {
        composable<WorkoutDestination.Home> {
            WorkoutRoute(
                onOpenHistory = { navController.navigate(WorkoutDestination.History) },
            )
        }
        composable<WorkoutDestination.History> {
            WorkoutHistoryRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<WorkoutDestination.Library> {
            ExerciseLibraryRoute(
                onNavigateBack = { navController.popBackStack() },
                onExerciseSelected = { _ -> },
                onCreateExercise = {}
            )
        }
    }
}

@Serializable
sealed interface WorkoutDestination {
    @Serializable
    data object Home : WorkoutDestination

    @Serializable
    data object History : WorkoutDestination

    @Serializable
    data object Library : WorkoutDestination
}
