package com.tnyx.features.workout.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.workout.presentation.library.widgets.LibraryActionBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onSearchClick: () -> Unit = {},
    onCreateProgramClick: () -> Unit = {},
    onCreateRoutineClick: () -> Unit = {},
    onCreateExerciseClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showActionBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = TnyxTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TnyxTheme.colors.textPrimary,
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search exercises",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showActionBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Open creation menu",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.surface,
                    scrolledContainerColor = TnyxTheme.colors.surfaceContainerHigh,
                )
            )
        },
        containerColor = TnyxTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TnyxTheme.colors.background)
        )

        // Creation Menu Bottom Sheet (+ Icon Trigger)
        LibraryActionBottomSheet(
            visible = showActionBottomSheet,
            onDismissRequest = { showActionBottomSheet = false },
            onCreateProgramClick = onCreateProgramClick,
            onCreateRoutineClick = onCreateRoutineClick,
            onCreateExerciseClick = onCreateExerciseClick
        )
    }
}
