package com.tnyx.features.workout.presentation.library.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Share
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.tnyx.features.workout.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.core.ui.components.inputs.TnyxTextFieldVariant
import com.tnyx.shared.workout.domain.catalog.ExerciseCatalogDto
import com.tnyx.shared.workout.domain.logic.ExerciseMediaResolver
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaPreference

@Composable
fun SearchExercisesScreen(
    state: SearchExercisesUiState,
    onAction: (SearchExercisesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Scroll-aware filter bar state ─────────────────────────────────────
    var filterBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Scroll down (negative y) → hide filter bar
                // Scroll up   (positive y) → show filter bar
                if (available.y < -2f) filterBarVisible = false
                else if (available.y > 2f) filterBarVisible = true
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        SearchExercisesTopBar(
            state = state,
            onAction = onAction,
        )

        // ── Collapsing Filter Bar ─────────────────────────────────────────
        AnimatedVisibility(
            visible = filterBarVisible,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            FilterChipsRow(
                filters = state.availableFilters,
                selectedFilter = state.selectedFilter,
                onFilterSelected = { onAction(SearchExercisesAction.FilterSelected(it)) },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background)
                .nestedScroll(nestedScrollConnection),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TnyxTheme.colors.accent,
                )
            } else if (state.viewType == ExerciseViewType.GRID) {
                // ── GRID MODE ──────────────────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = TnyxDimens.SpaceSM,
                        end = TnyxDimens.SpaceSM,
                        bottom = TnyxDimens.SpaceM
                    ),
                    horizontalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceS),
                    verticalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceS),
                ) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }, key = "header_row") {
                        val headerTitle = when {
                            state.searchQuery.isNotBlank() -> "Search Results"
                            state.selectedFilter.equals("FAVORITES", ignoreCase = true) -> "Favorite Exercises"
                            state.selectedFilter.equals("ALL", ignoreCase = true) -> "All Exercises"
                            else -> state.selectedFilter.replace("_", " ").lowercase().split(" ")
                                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                        }
                        ExercisesHeaderRow(
                            title = headerTitle,
                            viewType = state.viewType,
                            onToggleViewType = { onAction(SearchExercisesAction.ToggleViewType) },
                            showToggle = state.exercises.isNotEmpty(),
                        )
                    }

                    if (state.exercises.isEmpty()) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }, key = "empty_state") {
                            EmptyStateView(
                                onClearFilters = {
                                    onAction(SearchExercisesAction.FilterSelected("ALL"))
                                    onAction(SearchExercisesAction.SearchModeDismissed)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = TnyxDimens.SpaceXL)
                            )
                        }
                    } else {
                        items(items = state.exercises, key = { it.id }) { exercise ->
                            ExerciseGridItem(
                                exercise = exercise,
                                onInfoClick = { onAction(SearchExercisesAction.ExerciseInfoClicked(exercise.id)) },
                                onCardClick = { onAction(SearchExercisesAction.ExerciseSelected(exercise.id)) },
                                onLongCardClick = { onAction(SearchExercisesAction.ExerciseLongClicked(exercise.id)) },
                            )
                        }
                    }
                }
            } else {
                // ── LIST MODE ──────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = TnyxDimens.SpaceSM,
                        end = TnyxDimens.SpaceSM,
                        bottom = TnyxDimens.SpaceM
                    ),
                    verticalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceS),
                ) {
                    item(key = "header_row") {
                        val headerTitle = when {
                            state.searchQuery.isNotBlank() -> "Search Results"
                            state.selectedFilter.equals("FAVORITES", ignoreCase = true) -> "Favorite Exercises"
                            state.selectedFilter.equals("ALL", ignoreCase = true) -> "All Exercises"
                            else -> state.selectedFilter.replace("_", " ").lowercase().split(" ")
                                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                        }
                        ExercisesHeaderRow(
                            title = headerTitle,
                            viewType = state.viewType,
                            onToggleViewType = { onAction(SearchExercisesAction.ToggleViewType) },
                            showToggle = state.exercises.isNotEmpty(),
                        )
                    }

                    if (state.exercises.isEmpty()) {
                        item(key = "empty_state") {
                            EmptyStateView(
                                onClearFilters = {
                                    onAction(SearchExercisesAction.FilterSelected("ALL"))
                                    onAction(SearchExercisesAction.SearchModeDismissed)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = TnyxDimens.SpaceXL)
                            )
                        }
                    } else {
                        items(items = state.exercises, key = { it.id }) { exercise ->
                            ExerciseCardItem(
                                exercise = exercise,
                                onInfoClick = { onAction(SearchExercisesAction.ExerciseInfoClicked(exercise.id)) },
                                onCardClick = { onAction(SearchExercisesAction.ExerciseSelected(exercise.id)) },
                                onLongCardClick = { onAction(SearchExercisesAction.ExerciseLongClicked(exercise.id)) },
                            )
                        }
                    }
                }
            }
        }

        ExerciseActionsBottomSheet(
            exercise = state.selectedExerciseForActions,
            onDismiss = { onAction(SearchExercisesAction.ExerciseActionsDismissed) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Grid Card Item  (image fills card top, title + muscle group below)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseGridItem(
    exercise: ExerciseDefinition,
    onInfoClick: () -> Unit,
    onCardClick: () -> Unit,
    onLongCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedMedia = ExerciseMediaResolver.resolve(
        exercise = exercise,
        preference = ExerciseMediaPreference.AUTO,
    )
    val mediaAsset = resolvedMedia.asset ?: exercise.mediaAssets.firstOrNull()
    val imageUrl = mediaAsset?.thumbnailRef ?: mediaAsset?.imageRef ?: mediaAsset?.videoRef

    TnyxCard(
        modifier = modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Surface,
        shape = RoundedCornerShape(TnyxDimens.RadiusM),
        padding = TnyxDimens.SpaceNone,
        onClick = onCardClick,
        onLongClick = onLongCardClick,
    ) {
        Column {
            // Square image area — top of card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(TnyxTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Icon(
                                imageVector = Icons.Outlined.FitnessCenter,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textMuted,
                                modifier = Modifier.size(TnyxDimens.IconL),
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Outlined.FitnessCenter,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textMuted,
                                modifier = Modifier.size(TnyxDimens.IconL),
                            )
                        },
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = TnyxTheme.colors.accent,
                        modifier = Modifier.size(TnyxDimens.IconXL),
                    )
                }

                // Favorite / Bookmark button — top-left overlay (UI only)
                IconButton(
                    onClick = { /* UI placeholder for favorite feature */ },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(TnyxDimens.ScreenHeaderActionSize),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Favorite Exercise",
                        tint = TnyxTheme.colors.textMuted,
                        modifier = Modifier.size(TnyxDimens.IconS),
                    )
                }

                // Info button — top-right overlay
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(TnyxDimens.ScreenHeaderActionSize),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Exercise Info",
                        tint = TnyxTheme.colors.textMuted,
                        modifier = Modifier.size(TnyxDimens.IconS),
                    )
                }
            }

            // Title + muscle label below image
            Column(
                modifier = Modifier.padding(horizontal = TnyxDimens.SpaceSM, vertical = TnyxDimens.SpaceS),
            ) {
                Text(
                    text = exercise.name,
                    style = TnyxTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TnyxTheme.colors.textPrimary,
                    ),
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(TnyxDimens.SpaceXXS))
                val primaryMuscle = exercise.primaryMuscleGroups.firstOrNull()
                    ?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "General"
                Text(
                    text = primaryMuscle,
                    style = TnyxTheme.typography.bodySmall.copy(
                        color = TnyxTheme.colors.textSecondary,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List Card Item  (horizontal row — thumbnail + name + info icon)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCardItem(
    exercise: ExerciseDefinition,
    onInfoClick: () -> Unit,
    onCardClick: () -> Unit,
    onLongCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedMedia = ExerciseMediaResolver.resolve(
        exercise = exercise,
        preference = ExerciseMediaPreference.AUTO,
    )
    val mediaAsset = resolvedMedia.asset ?: exercise.mediaAssets.firstOrNull()
    val thumbnailUrl = mediaAsset?.thumbnailRef ?: mediaAsset?.imageRef ?: mediaAsset?.videoRef

    TnyxCard(
        modifier = modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Surface,
        shape = RoundedCornerShape(TnyxDimens.RadiusM),
        padding = TnyxDimens.SpaceS,
        onClick = onCardClick,
        onLongClick = onLongCardClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(TnyxDimens.RadiusS))
                    .background(TnyxTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbnailUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = thumbnailUrl,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Icon(
                                imageVector = Icons.Outlined.FitnessCenter,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textMuted,
                                modifier = Modifier.size(TnyxDimens.IconM)
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Outlined.FitnessCenter,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textMuted,
                                modifier = Modifier.size(TnyxDimens.IconM)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = TnyxTheme.colors.accent,
                        modifier = Modifier.size(TnyxDimens.IconL)
                    )
                }
            }

            Spacer(modifier = Modifier.width(TnyxDimens.SpaceSM))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = exercise.name,
                    style = TnyxTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TnyxTheme.colors.textPrimary
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(TnyxDimens.SpaceXS))

                val primaryMuscle = exercise.primaryMuscleGroups.firstOrNull()?.replace("_", " ")?.lowercase()
                    ?.replaceFirstChar { it.uppercase() } ?: "General"
                val equipment = exercise.equipment.firstOrNull()?.replace("_", " ")?.lowercase()
                    ?.replaceFirstChar { it.uppercase() } ?: "Equipment"

                Text(
                    text = "$primaryMuscle • $equipment",
                    style = TnyxTheme.typography.bodySmall.copy(
                        color = TnyxTheme.colors.textSecondary
                    ),
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .size(TnyxDimens.ScreenHeaderActionSize)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Exercise Info",
                    tint = TnyxTheme.colors.textMuted
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter Chips Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FilterChipsRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TnyxDimens.SpaceXS),
        contentPadding = PaddingValues(horizontal = TnyxDimens.SpaceSM),
        horizontalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceS)
    ) {
        if (!selectedFilter.equals("ALL", ignoreCase = true)) {
            item {
                FilterChip(
                    selected = false,
                    onClick = { onFilterSelected("ALL") },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Filter",
                                tint = TnyxTheme.colors.accent,
                                modifier = Modifier.size(TnyxDimens.IconXS)
                            )
                            Spacer(modifier = Modifier.width(TnyxDimens.SpaceXXS))
                            Text(
                                text = "Clear",
                                style = TnyxTheme.typography.labelMedium.copy(
                                    color = TnyxTheme.colors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = TnyxTheme.colors.accent.copy(alpha = 0.15f),
                        labelColor = TnyxTheme.colors.accent
                    ),
                    shape = RoundedCornerShape(TnyxDimens.RadiusXL),
                    border = null
                )
            }
        }

        item {
            val isFavoriteSelected = "FAVORITES".equals(selectedFilter, ignoreCase = true)
            FilterChip(
                selected = isFavoriteSelected,
                onClick = { onFilterSelected("FAVORITES") },
                label = {
                    Icon(
                        imageVector = if (isFavoriteSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Favorites",
                        tint = if (isFavoriteSelected) TnyxTheme.colors.onPrimary else TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(TnyxDimens.IconS)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TnyxTheme.colors.surface,
                    selectedContainerColor = TnyxTheme.colors.accent,
                    labelColor = TnyxTheme.colors.textPrimary,
                    selectedLabelColor = TnyxTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(TnyxDimens.RadiusXL),
                border = null
            )
        }

        items(filters) { filter ->
            val isSelected = filter.equals(selectedFilter, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.replace("_", " "),
                        style = TnyxTheme.typography.labelMedium.copy(
                            color = if (isSelected) TnyxTheme.colors.onPrimary else TnyxTheme.colors.textPrimary
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TnyxTheme.colors.surface,
                    selectedContainerColor = TnyxTheme.colors.accent,
                    labelColor = TnyxTheme.colors.textPrimary,
                    selectedLabelColor = TnyxTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(TnyxDimens.RadiusXL),
                border = null
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateView(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(TnyxDimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            tint = TnyxTheme.colors.textMuted,
            modifier = Modifier.size(TnyxDimens.IconXL)
        )
        Spacer(modifier = Modifier.height(TnyxDimens.SpaceSM))
        Text(
            text = "No exercises found",
            style = TnyxTheme.typography.titleMedium.copy(
                color = TnyxTheme.colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(TnyxDimens.SpaceM))
        TnyxSecondaryButton(
            text = "Clear Filters",
            onPressed = onClearFilters,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Exercises Header Row — title (left) + view toggle (right)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExercisesHeaderRow(
    title: String,
    viewType: ExerciseViewType,
    onToggleViewType: () -> Unit,
    showToggle: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = TnyxTheme.typography.titleMedium.copy(
                color = TnyxTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (showToggle) {
            IconButton(
                onClick = onToggleViewType,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = if (viewType == ExerciseViewType.LIST)
                        painterResource(R.drawable.ic_grid_view)
                    else
                        painterResource(R.drawable.ic_list_view),
                    contentDescription = if (viewType == ExerciseViewType.LIST) "Switch to Grid" else "Switch to List",
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(TnyxDimens.IconS),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchExercisesTopBar(
    state: SearchExercisesUiState,
    onAction: (SearchExercisesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = TnyxTheme.colors.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = TnyxDimens.SpaceXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (state.isSearchActive) {
                        onAction(SearchExercisesAction.SearchModeDismissed)
                    } else {
                        onAction(SearchExercisesAction.BackClicked)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (state.isSearchActive) "Dismiss Search" else "Back",
                    tint = TnyxTheme.colors.textPrimary
                )
            }

            if (state.isSearchActive) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    TnyxTextField(
                        value = state.searchQuery,
                        onValueChange = { onAction(SearchExercisesAction.QueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search exercises...",
                                style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textMuted)
                            )
                        },
                        variant = TnyxTextFieldVariant.Compact,
                        trailingIcon = if (state.searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onAction(SearchExercisesAction.QueryChanged("")) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TnyxTheme.colors.textMuted
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {})
                    )
                }
            } else {
                Text(
                    text = "Exercises",
                    style = TnyxTheme.typography.titleLarge.copy(
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.weight(1f),
                )

                IconButton(onClick = { onAction(SearchExercisesAction.SearchIconClicked) }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = TnyxTheme.colors.textPrimary
                    )
                }
            }

            IconButton(onClick = { onAction(SearchExercisesAction.FilterIconClicked) }) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = TnyxTheme.colors.textPrimary
                )
            }

            if (!state.isSearchActive) {
                IconButton(onClick = { onAction(SearchExercisesAction.CreateIconClicked) }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Create Custom Exercise",
                        tint = TnyxTheme.colors.accent
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Exercise Long-Press Actions Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseActionsBottomSheet(
    exercise: ExerciseDefinition?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TnyxModalBottomSheet(
        visible = exercise != null,
        onDismissRequest = onDismiss,
        showDivider = false,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TnyxDimens.SpaceXS)
        ) {
            ExerciseActionItem(
                icon = Icons.Outlined.Share,
                title = "Share Exercise",
                onClick = { onDismiss() }
            )
            ExerciseActionItem(
                icon = Icons.Outlined.FitnessCenter,
                title = "About Exercise",
                onClick = { onDismiss() }
            )
            ExerciseActionItem(
                icon = Icons.Outlined.BookmarkBorder,
                title = "In Favorites",
                onClick = { onDismiss() }
            )
            ExerciseActionItem(
                icon = Icons.Outlined.CreateNewFolder,
                title = "Add to folder",
                onClick = { onDismiss() }
            )
        }
    }
}

@Composable
private fun ExerciseActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = TnyxDimens.SpaceS, vertical = TnyxDimens.SpaceSM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TnyxTheme.colors.textPrimary,
            modifier = Modifier.size(TnyxDimens.IconM)
        )
        Spacer(modifier = Modifier.width(TnyxDimens.SpaceM))
        Text(
            text = title,
            style = TnyxTheme.typography.bodyLarge.copy(
                color = TnyxTheme.colors.textPrimary,
                fontWeight = FontWeight.Normal
            )
        )
    }
}
