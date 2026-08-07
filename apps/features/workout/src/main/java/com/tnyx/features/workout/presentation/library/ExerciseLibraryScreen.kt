package com.tnyx.features.workout.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.workout.presentation.library.widgets.LibraryActionBottomSheet

@Composable
fun ExerciseLibraryScreen(
    onSearchClick: () -> Unit = {},
    onCreateProgramClick: () -> Unit = {},
    onCreateRoutineClick: () -> Unit = {},
    onCreateExerciseClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showActionBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
    ) {
        TnyxScreenHeader(
            title = "Library",
            size = TnyxHeaderSize.Compact,
            uppercaseTitle = false,
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
            }
        )

        // Library content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background)
        )
    }

    // Creation Menu Bottom Sheet (+ Icon Trigger)
    LibraryActionBottomSheet(
        visible = showActionBottomSheet,
        onDismissRequest = { showActionBottomSheet = false },
        onCreateProgramClick = onCreateProgramClick,
        onCreateRoutineClick = onCreateRoutineClick,
        onCreateExerciseClick = onCreateExerciseClick
    )
}
