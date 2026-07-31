package com.tnyx.features.workout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout",
                        style = TnyxTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TnyxTheme.colors.textPrimary,
                    )
                },
                actions = {
                    IconButton(onClick = { onAction(WorkoutAction.HistoryClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Workout History",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { onAction(WorkoutAction.LibraryClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = "Exercise Library",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                )
            )
        },
        containerColor = TnyxTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TnyxPrimaryButton(
                text = "Open Exercise Library",
                onPressed = { onAction(WorkoutAction.LibraryClicked) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WorkoutHistoryScreen(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    )
}
