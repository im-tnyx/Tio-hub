package com.tnyx.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.rememberColumnState
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

    MaterialTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    val columnState = rememberColumnState()
                    ScreenScaffold(scrollState = columnState) {
                        ScalingLazyColumn(
                            columnState = columnState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item { Text("Tnyx Home") }
                            item { Text("Step 1 Complete 🚀") }
                        }
                    }
                }
            }
        }
    }
}
