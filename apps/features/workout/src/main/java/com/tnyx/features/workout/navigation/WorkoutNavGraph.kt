package com.tnyx.features.workout.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tnyx.features.workout.presentation.WorkoutHistoryRoute
import com.tnyx.features.workout.presentation.WorkoutRoute
import com.tnyx.features.workout.presentation.library.ExerciseLibraryRoute
import com.tnyx.features.workout.presentation.library.createexercise.CreateExerciseRoute
import com.tnyx.features.workout.presentation.library.exercises.SearchExercisesRoute
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
                onOpenLibrary = { navController.navigate(WorkoutDestination.Library) },
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
        composable<WorkoutDestination.SearchExercises>(
            enterTransition = { slideInVertically(animationSpec = tween(300)) { it } },
            popExitTransition = { slideOutVertically(animationSpec = tween(300)) { it } },
        ) {
            SearchExercisesRoute(
                onNavigateBack = { navController.popBackStack() },
                onCreateClick = {
                    navController.navigate(WorkoutDestination.CreateExercise)
                }
            )
        }
        composable<WorkoutDestination.CreateExercise> {
            CreateExerciseRoute(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
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

    @Serializable
    data object SearchExercises : WorkoutDestination

    @Serializable
    data object CreateExercise : WorkoutDestination
}
