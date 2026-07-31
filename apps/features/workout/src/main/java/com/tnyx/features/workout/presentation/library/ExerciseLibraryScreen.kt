package com.tnyx.features.workout.presentation.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    state: ExerciseLibraryUiState,
    onAction: (ExerciseLibraryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bodyParts = listOf("ALL", "CHEST", "BACK", "LEGS", "SHOULDERS", "ARMS", "CORE")

    Scaffold(
        topBar = {
            if (state.isSearchActive) {
                // Expanded Search Bar Mode
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { onAction(ExerciseLibraryAction.SearchQueryChanged(it)) },
                            placeholder = {
                                Text(
                                    "Search exercises...",
                                    style = TnyxTheme.typography.bodyMedium,
                                    color = TnyxTheme.colors.textSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = TnyxTheme.colors.textSecondary
                                )
                            },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onAction(ExerciseLibraryAction.SearchQueryChanged("")) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = "Clear search",
                                            tint = TnyxTheme.colors.textSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.3f),
                                unfocusedBorderColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f),
                                focusedContainerColor = TnyxTheme.colors.surfaceVariant,
                                unfocusedContainerColor = TnyxTheme.colors.surfaceVariant,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onAction(ExerciseLibraryAction.SearchIconClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search",
                                tint = TnyxTheme.colors.textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TnyxTheme.colors.background,
                    )
                )
            } else {
                // Standard Top Bar: Left Title "Library", Right Search + Plus Icons
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
                        IconButton(onClick = { onAction(ExerciseLibraryAction.SearchIconClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search exercises",
                                tint = TnyxTheme.colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { onAction(ExerciseLibraryAction.CreateExerciseClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Create exercise",
                                tint = TnyxTheme.colors.textPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TnyxTheme.colors.background,
                    )
                )
            }
        },
        containerColor = TnyxTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Body Part Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bodyParts) { bodyPart ->
                    val isSelected = state.selectedBodyPart == bodyPart
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAction(ExerciseLibraryAction.BodyPartSelected(bodyPart)) },
                        label = {
                            Text(
                                text = bodyPart,
                                style = TnyxTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TnyxTheme.colors.textPrimary,
                            selectedLabelColor = TnyxTheme.colors.background,
                            containerColor = TnyxTheme.colors.surfaceVariant,
                            labelColor = TnyxTheme.colors.textPrimary,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Exercise List
            if (state.exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isNotEmpty()) "No exercises match '${state.searchQuery}'" else "No exercises available in library.",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.exercises, key = { it.id }) { exercise ->
                        ExerciseLibraryItemCard(
                            exercise = exercise,
                            onFavoriteToggle = { onAction(ExerciseLibraryAction.FavoriteToggled(exercise.id)) },
                            onClick = { onAction(ExerciseLibraryAction.ExerciseClicked(exercise.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseLibraryItemCard(
    exercise: ExerciseLibraryUiItem,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TnyxTheme.colors.surface,
        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TnyxTheme.colors.surfaceVariant,
                    ) {
                        Text(
                            text = exercise.bodyPart,
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TnyxTheme.colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TnyxTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                    ) {
                        Text(
                            text = exercise.category,
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TnyxTheme.colors.textMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (exercise.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (exercise.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (exercise.isFavorite) Color(0xFFE53935) else TnyxTheme.colors.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
