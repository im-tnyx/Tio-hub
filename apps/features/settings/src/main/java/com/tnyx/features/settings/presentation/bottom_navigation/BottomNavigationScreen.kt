package com.tnyx.features.settings.presentation.bottom_navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.tnyx.core.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.shell.domain.model.MAX_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.MIN_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab
import kotlin.math.roundToInt

private const val DEFAULT_PREVIEW_ICON_SCALE = 1f
private const val DRAGGED_PREVIEW_ICON_SCALE = 1.2f
private const val DRAG_OVERLAY_Z_INDEX = 1f
private const val AVAILABLE_TAB_GRID_COLUMNS = 4

private enum class TabDragSource {
    Preview,
    Available,
}

private data class TabDragState(
    val tab: ShellTab,
    val source: TabDragSource,
    val position: Offset,
)

@Composable
fun BottomNavigationScreen(
    state: BottomNavigationUiState,
    onAction: (BottomNavigationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var previewBounds by remember { mutableStateOf<Rect?>(null) }
    var tabsDropBounds by remember { mutableStateOf<Rect?>(null) }
    var dragState by remember { mutableStateOf<TabDragState?>(null) }

    val startDrag: (ShellTab, TabDragSource, Offset) -> Unit = { tab, source, position ->
        if (tab != ShellTab.Home) {
            dragState = TabDragState(tab = tab, source = source, position = position)
        }
    }
    val updateDrag: (Offset) -> Unit = { position ->
        dragState = dragState?.copy(position = position)
    }
    val finishDrag: () -> Unit = {
        val completedDrag = dragState
        if (completedDrag != null) {
            when (completedDrag.source) {
                TabDragSource.Preview -> {
                    if (
                        tabsDropBounds?.contains(completedDrag.position) == true &&
                        state.draftTabs.size > MIN_BOTTOM_NAV_TABS
                    ) {
                        onAction(BottomNavigationAction.ToggleTab(completedDrag.tab))
                    }
                }
                TabDragSource.Available -> {
                    val bounds = previewBounds
                    if (
                        bounds?.contains(completedDrag.position) == true &&
                        state.draftTabs.size < MAX_BOTTOM_NAV_TABS
                    ) {
                        onAction(
                            BottomNavigationAction.AddTab(
                                tab = completedDrag.tab,
                                targetIndex = previewDropIndex(
                                    bounds = bounds,
                                    dropX = completedDrag.position.x,
                                    currentTabCount = state.draftTabs.size,
                                ),
                            )
                        )
                    }
                }
            }
        }
        dragState = null
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                EditorTopBar(onBack = { onAction(BottomNavigationAction.BackClicked) })
            },
            bottomBar = {
                EditorBottomBar(
                    canReset = state.canReset,
                    canSave = state.canSave,
                    isSaving = state.isSaving,
                    onReset = { onAction(BottomNavigationAction.ResetClicked) },
                    onSave = { onAction(BottomNavigationAction.SaveClicked) },
                )
            },
            containerColor = TnyxTheme.colors.background,
        ) { padding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TnyxTheme.colors.warning)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    Box(
                        modifier = Modifier.padding(
                            horizontal = TnyxTheme.dimens.SpaceM,
                            vertical = TnyxTheme.dimens.SpaceSM,
                        ),
                    ) {
                        NavigationPreview(
                            tabs = state.draftTabs,
                            onBoundsChanged = { previewBounds = it },
                            onDragStarted = { tab, position ->
                                startDrag(tab, TabDragSource.Preview, position)
                            },
                            onDragMoved = updateDrag,
                            onDragFinished = finishDrag,
                            onRemoveTab = { tab ->
                                onAction(BottomNavigationAction.ToggleTab(tab))
                            },
                            onMoveTab = { tab, targetIndex ->
                                onAction(BottomNavigationAction.MoveTab(tab, targetIndex))
                            },
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = TnyxTheme.dimens.SpaceM,
                            end = TnyxTheme.dimens.SpaceM,
                            bottom = TnyxTheme.dimens.SpaceSM,
                        ),
                        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
                    ) {
                        item {
                            NavigationModeSelector(
                                selectedMode = state.selectedMode,
                                onModeSelected = { mode ->
                                    onAction(BottomNavigationAction.ApplyMode(mode))
                                },
                            )
                        }

                        state.errorMessage?.let { message ->
                            item {
                                ErrorMessage(
                                    message = message,
                                    onDismiss = {
                                        onAction(BottomNavigationAction.DismissError)
                                    },
                                )
                            }
                        }

                        item {
                            SectionHeader(
                                title = "AVAILABLE TABS",
                                detail = "${state.draftTabs.size}/$MAX_BOTTOM_NAV_TABS active",
                            )
                        }

                        item {
                            TnyxCard(
                                modifier = Modifier.onGloballyPositioned {
                                    tabsDropBounds = it.boundsInRoot()
                                },
                                variant = TnyxCardVariant.Normal,
                                padding = TnyxTheme.dimens.SpaceSM,
                            ) {
                                AvailableTabsGrid(
                                    tabs = state.supportedTabs,
                                    selectedTabs = state.draftTabs.toSet(),
                                    canAdd = state.draftTabs.size < MAX_BOTTOM_NAV_TABS,
                                    canRemove = state.draftTabs.size > MIN_BOTTOM_NAV_TABS,
                                    draggedTab = dragState?.takeIf {
                                        it.source == TabDragSource.Available
                                    }?.tab,
                                    onDragStarted = { tab, position ->
                                        startDrag(tab, TabDragSource.Available, position)
                                    },
                                    onDragMoved = updateDrag,
                                    onDragFinished = finishDrag,
                                    onAdd = { tab ->
                                        onAction(BottomNavigationAction.ToggleTab(tab))
                                    },
                                    onRemove = { tab ->
                                        onAction(BottomNavigationAction.ToggleTab(tab))
                                    },
                                )
                            }
                        }

                        item {
                            Text(
                                text = "All tabs stay visible. Use + to add, X to remove, or drag between Available tabs and Preview.",
                                style = TnyxTheme.typography.labelSmall,
                                color = TnyxTheme.colors.textMuted,
                            )
                        }

                        item { Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS)) }
                    }
                }
            }
        }

        if (state.showDiscardDialog) {
            DiscardChangesDialog(
                onKeepEditing = {
                    onAction(BottomNavigationAction.KeepEditingClicked)
                },
                onDiscard = {
                    onAction(BottomNavigationAction.DiscardChangesClicked)
                },
            )
        }

        dragState?.let { activeDrag ->
            DraggedTabOverlay(dragState = activeDrag)
        }
    }
}

@Composable
private fun EditorTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
            .padding(
                horizontal = TnyxTheme.dimens.SpaceS,
                vertical = TnyxTheme.dimens.SpaceS,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TnyxTheme.colors.textPrimary,
            )
        }
        Text(
            text = "Bottom navigation",
            style = TnyxTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun EditorBottomBar(
    canReset: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    onReset: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        color = TnyxTheme.colors.surface,
        shadowElevation = TnyxTheme.elevation.Level4,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = TnyxTheme.dimens.SpaceM,
                    vertical = TnyxTheme.dimens.SpaceSM,
                ),
            horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM),
        ) {
            OutlinedButton(
                onClick = onReset,
                enabled = canReset,
                modifier = Modifier.weight(1f),
            ) {
                Text("Reset")
            }
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TnyxTheme.colors.warning,
                    contentColor = TnyxTheme.colors.background,
                ),
            ) {
                Text(if (isSaving) "Saving…" else "Save")
            }
        }
    }
}

@Composable
private fun NavigationPreview(
    tabs: List<ShellTab>,
    onBoundsChanged: (Rect) -> Unit,
    onDragStarted: (ShellTab, Offset) -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragFinished: () -> Unit,
    onRemoveTab: (ShellTab) -> Unit,
    onMoveTab: (ShellTab, Int) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentTabs by rememberUpdatedState(tabs)
    val currentOnBoundsChanged by rememberUpdatedState(onBoundsChanged)
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnDragMoved by rememberUpdatedState(onDragMoved)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    val currentOnMoveTab by rememberUpdatedState(onMoveTab)
    var previewWidthPx by remember { mutableIntStateOf(0) }
    var previewOrigin by remember { mutableStateOf(Offset.Zero) }
    var draggedTab by remember { mutableStateOf<ShellTab?>(null) }

    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = TnyxTheme.dimens.SpaceSM,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "PREVIEW",
                    style = TnyxTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textMuted,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Long-press and drag",
                    style = TnyxTheme.typography.labelSmall,
                    color = TnyxTheme.colors.textMuted,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = TnyxTheme.components.navigation.bottomNavHeight)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        previewWidthPx = coordinates.size.width
                        previewOrigin = bounds.topLeft
                        currentOnBoundsChanged(bounds)
                    }
                    .pointerInput(Unit) {
                        var activeTab: ShellTab? = null
                        var workingTabs = emptyList<ShellTab>()

                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                workingTabs = currentTabs
                                if (previewWidthPx <= 0 || workingTabs.isEmpty()) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                val slotWidth = previewWidthPx.toFloat() / workingTabs.size
                                val startIndex = (offset.x / slotWidth)
                                    .toInt()
                                    .coerceIn(0, workingTabs.lastIndex)
                                val candidate = workingTabs[startIndex]
                                if (candidate != ShellTab.Home) {
                                    activeTab = candidate
                                    draggedTab = candidate
                                    currentOnDragStarted(
                                        candidate,
                                        previewOrigin + offset,
                                    )
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                }
                            },
                            onDragCancel = {
                                if (activeTab != null) currentOnDragFinished()
                                activeTab = null
                                draggedTab = null
                            },
                            onDragEnd = {
                                if (activeTab != null) currentOnDragFinished()
                                activeTab = null
                                draggedTab = null
                            },
                            onDrag = { change, _ ->
                                val tab = activeTab ?: return@detectDragGesturesAfterLongPress
                                if (previewWidthPx <= 0 || workingTabs.size <= 1) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                change.consume()
                                currentOnDragMoved(previewOrigin + change.position)
                                val dragPositionX = change.position.x
                                    .coerceIn(0f, previewWidthPx.toFloat())
                                val slotWidth = previewWidthPx.toFloat() / workingTabs.size
                                val targetIndex = (dragPositionX / slotWidth)
                                    .toInt()
                                    .coerceIn(1, workingTabs.lastIndex)
                                if (workingTabs.indexOf(tab) != targetIndex) {
                                    currentOnMoveTab(tab, targetIndex)
                                    workingTabs = moveBottomNavigationTab(
                                        tabs = workingTabs,
                                        tab = tab,
                                        targetIndex = targetIndex,
                                    )
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.TextHandleMove
                                    )
                                }
                            },
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    PreviewTab(
                        tab = tab,
                        index = index,
                        lastIndex = tabs.lastIndex,
                        isDragging = tab == draggedTab,
                        canRemove = tabs.size > MIN_BOTTOM_NAV_TABS,
                        onRemoveTab = onRemoveTab,
                        onMoveTab = onMoveTab,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewTab(
    tab: ShellTab,
    index: Int,
    lastIndex: Int,
    isDragging: Boolean,
    canRemove: Boolean,
    onRemoveTab: (ShellTab) -> Unit,
    onMoveTab: (ShellTab, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (isDragging) {
            DRAGGED_PREVIEW_ICON_SCALE
        } else {
            DEFAULT_PREVIEW_ICON_SCALE
        },
        animationSpec = tween(durationMillis = TnyxTheme.motion.DurationShort1),
        label = "previewIconDragScale",
    )
    val accessibilityActions = remember(
        tab,
        index,
        lastIndex,
        canRemove,
        onMoveTab,
        onRemoveTab,
    ) {
        buildList {
            if (tab != ShellTab.Home && index > 1) {
                add(
                    CustomAccessibilityAction("Move ${tab.displayLabel()} left") {
                        onMoveTab(tab, index - 1)
                        true
                    }
                )
            }
            if (tab != ShellTab.Home && index < lastIndex) {
                add(
                    CustomAccessibilityAction("Move ${tab.displayLabel()} right") {
                        onMoveTab(tab, index + 1)
                        true
                    }
                )
            }
            if (tab != ShellTab.Home && canRemove) {
                add(
                    CustomAccessibilityAction("Remove ${tab.displayLabel()}") {
                        onRemoveTab(tab)
                        true
                    }
                )
            }
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = TnyxTheme.dimens.SpaceXXS)
            .padding(vertical = TnyxTheme.dimens.SpaceS)
            .semantics(mergeDescendants = true) {
                contentDescription = if (tab == ShellTab.Home) {
                    "Home, fixed first"
                } else {
                    "${tab.displayLabel()}, position ${index + 1}. Long-press and drag to reorder"
                }
                customActions = accessibilityActions
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS),
    ) {
        Box(
            modifier = Modifier
                .size(TnyxTheme.dimens.IconL)
                .then(
                    if (isDragging) {
                        Modifier.background(
                            color = TnyxTheme.colors.surfaceVariant,
                            shape = TnyxTheme.shapes.Circle,
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            TabIcon(
                tab = tab,
                highlighted = isDragging,
                modifier = Modifier
                    .size(TnyxTheme.dimens.IconS)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            )
        }
        Text(
            text = tab.displayLabel(),
            style = TnyxTheme.typography.labelSmall,
            color = TnyxTheme.colors.textSecondary,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NavigationModeSelector(
    selectedMode: BottomNavigationMode,
    onModeSelected: (BottomNavigationMode) -> Unit,
) {
    val visibleModes = if (selectedMode == BottomNavigationMode.Custom) {
        BottomNavigationMode.entries
    } else {
        BottomNavigationMode.entries.filterNot { it == BottomNavigationMode.Custom }
    }

    Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
        SectionHeader(title = "NAVIGATION MODE")
        TnyxCard(
            variant = TnyxCardVariant.Normal,
            padding = TnyxTheme.dimens.SpaceSM,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
                ) {
                    items(
                        items = visibleModes,
                        key = BottomNavigationMode::name,
                    ) { mode ->
                        val isCustom = mode == BottomNavigationMode.Custom
                        val isSelected = mode == selectedMode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (!isCustom) onModeSelected(mode)
                            },
                            enabled = !isCustom || isSelected,
                            label = { Text(mode.displayLabel()) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(TnyxTheme.dimens.IconXS),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
                Text(
                    text = if (selectedMode == BottomNavigationMode.Custom) {
                        "Custom is active because the selected tabs differ from a preset."
                    } else {
                        "Choose a preset, or add and remove tabs to create a Custom layout."
                    },
                    style = TnyxTheme.typography.labelSmall,
                    color = TnyxTheme.colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun AvailableTabsGrid(
    tabs: List<ShellTab>,
    selectedTabs: Set<ShellTab>,
    canAdd: Boolean,
    canRemove: Boolean,
    draggedTab: ShellTab?,
    onDragStarted: (ShellTab, Offset) -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragFinished: () -> Unit,
    onAdd: (ShellTab) -> Unit,
    onRemove: (ShellTab) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM)) {
        tabs.chunked(AVAILABLE_TAB_GRID_COLUMNS).forEach { rowTabs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
            ) {
                rowTabs.forEach { tab ->
                    AvailableTabTile(
                        tab = tab,
                        isSelected = tab in selectedTabs,
                        canAdd = canAdd,
                        canRemove = canRemove,
                        isDragging = tab == draggedTab,
                        onDragStarted = { position -> onDragStarted(tab, position) },
                        onDragMoved = onDragMoved,
                        onDragFinished = onDragFinished,
                        onAdd = { onAdd(tab) },
                        onRemove = { onRemove(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(AVAILABLE_TAB_GRID_COLUMNS - rowTabs.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AvailableTabTile(
    tab: ShellTab,
    isSelected: Boolean,
    canAdd: Boolean,
    canRemove: Boolean,
    isDragging: Boolean,
    onDragStarted: (Offset) -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragFinished: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPinned = tab == ShellTab.Home
    val actionEnabled = if (isSelected) {
        !isPinned && canRemove
    } else {
        canAdd
    }
    val actionDescription = when {
        isPinned -> "${tab.displayLabel()}, active in Preview and pinned"
        isSelected && canRemove -> "${tab.displayLabel()}, active in Preview. Tap X to remove"
        isSelected -> "${tab.displayLabel()}, active in Preview. Minimum tabs must remain"
        canAdd -> "${tab.displayLabel()}, available. Tap plus to add, or long-press and drag into Preview"
        else -> "${tab.displayLabel()}, available. Maximum tabs already active"
    }

    Column(
        modifier = modifier.padding(vertical = TnyxTheme.dimens.SpaceXS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS),
    ) {
        Box(
            modifier = Modifier
                .size(TnyxTheme.dimens.IconXL)
                .clip(TnyxTheme.shapes.Circle)
                .clickable(
                    enabled = actionEnabled,
                    role = Role.Button,
                    onClick = {
                        if (isSelected) onRemove() else onAdd()
                    },
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = actionDescription
                },
        ) {
            AvailableDraggableTabIcon(
                tab = tab,
                enabled = !isSelected && canAdd,
                isSelected = isSelected,
                isDragging = isDragging,
                onDragStarted = onDragStarted,
                onDragMoved = onDragMoved,
                onDragFinished = onDragFinished,
            )
            AvailableTabActionBadge(
                isPinned = isPinned,
                isSelected = isSelected,
                enabled = actionEnabled,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        Text(
            text = tab.displayLabel(),
            style = TnyxTheme.typography.labelMedium,
            color = if (isSelected) {
                TnyxTheme.colors.textMuted
            } else {
                TnyxTheme.colors.textPrimary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AvailableTabActionBadge(
    isPinned: Boolean,
    isSelected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(TnyxTheme.dimens.IconS)
            .background(
                color = if (enabled) {
                    TnyxTheme.colors.surfaceRaised
                } else {
                    TnyxTheme.colors.surfaceVariant
                },
                shape = TnyxTheme.shapes.Circle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = when {
                isPinned -> Icons.Rounded.Check
                isSelected -> Icons.Rounded.Close
                else -> Icons.Rounded.Add
            },
            contentDescription = null,
            tint = when {
                !enabled -> TnyxTheme.colors.textMuted
                isSelected -> TnyxTheme.colors.error
                else -> TnyxTheme.colors.warning
            },
            modifier = Modifier.size(TnyxTheme.dimens.IconXXS),
        )
    }
}

@Composable
private fun AvailableDraggableTabIcon(
    tab: ShellTab,
    enabled: Boolean,
    isSelected: Boolean,
    isDragging: Boolean,
    onDragStarted: (Offset) -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragFinished: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnDragMoved by rememberUpdatedState(onDragMoved)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    var iconBounds by remember { mutableStateOf(Rect.Zero) }
    val iconScale by animateFloatAsState(
        targetValue = if (isDragging) {
            DRAGGED_PREVIEW_ICON_SCALE
        } else {
            DEFAULT_PREVIEW_ICON_SCALE
        },
        animationSpec = tween(durationMillis = TnyxTheme.motion.DurationShort1),
        label = "availableIconDragScale",
    )

    Box(
        modifier = Modifier
            .size(TnyxTheme.dimens.IconXL)
            .background(
                color = TnyxTheme.colors.surfaceVariant,
                shape = TnyxTheme.shapes.Circle,
            )
            .onGloballyPositioned { iconBounds = it.boundsInRoot() }
            .pointerInput(tab, enabled) {
                if (!enabled) return@pointerInput

                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        currentOnDragStarted(iconBounds.topLeft + offset)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragCancel = currentOnDragFinished,
                    onDragEnd = currentOnDragFinished,
                    onDrag = { change, _ ->
                        change.consume()
                        currentOnDragMoved(iconBounds.topLeft + change.position)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        TabIcon(
            tab = tab,
            highlighted = isDragging,
            muted = isSelected,
            modifier = Modifier
                .size(TnyxTheme.dimens.IconM)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
        )
    }
}

@Composable
private fun DraggedTabOverlay(dragState: TabDragState) {
    val density = LocalDensity.current
    val overlaySize = TnyxTheme.dimens.IconXL
    val overlayRadiusPx = with(density) { overlaySize.toPx() / 2 }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (dragState.position.x - overlayRadiusPx).roundToInt(),
                    y = (dragState.position.y - overlayRadiusPx).roundToInt(),
                )
            }
            .zIndex(DRAG_OVERLAY_Z_INDEX)
            .size(overlaySize)
            .background(
                color = TnyxTheme.colors.surfaceVariant,
                shape = TnyxTheme.shapes.Circle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        TabIcon(
            tab = dragState.tab,
            highlighted = true,
            modifier = Modifier
                .size(TnyxTheme.dimens.IconM)
                .graphicsLayer {
                    scaleX = DRAGGED_PREVIEW_ICON_SCALE
                    scaleY = DRAGGED_PREVIEW_ICON_SCALE
                },
        )
    }
}

internal fun previewDropIndex(
    bounds: Rect,
    dropX: Float,
    currentTabCount: Int,
): Int {
    if (currentTabCount <= 1 || bounds.width <= 0f) return 1

    val finalTabCount = currentTabCount + 1
    val slotWidth = bounds.width / finalTabCount
    return ((dropX - bounds.left) / slotWidth)
        .toInt()
        .coerceIn(1, currentTabCount)
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = TnyxTheme.dimens.SpaceSM,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = message,
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    detail: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TnyxTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textMuted,
        )
        Spacer(modifier = Modifier.size(TnyxTheme.dimens.SpaceS))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TnyxTheme.components.card.outlinedBorderColor,
        )
        if (detail != null) {
            Spacer(modifier = Modifier.size(TnyxTheme.dimens.SpaceS))
            Text(
                text = detail,
                style = TnyxTheme.typography.labelSmall,
                color = TnyxTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun DiscardChangesDialog(
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        shape = TnyxTheme.shapes.Material.extraLarge,
        containerColor = TnyxTheme.colors.surfaceRaised,
        titleContentColor = TnyxTheme.colors.textPrimary,
        textContentColor = TnyxTheme.colors.textSecondary,
        tonalElevation = TnyxTheme.elevation.None,
        title = {
            Text(
                text = "Discard navigation changes?",
                style = TnyxTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = "Your preview has unsaved tab or order changes.",
                style = TnyxTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDiscard,
                shape = TnyxTheme.shapes.Material.medium,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TnyxTheme.colors.error,
                ),
            ) {
                Text(
                    text = "Discard",
                    style = TnyxTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onKeepEditing,
                shape = TnyxTheme.shapes.Material.medium,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TnyxTheme.colors.textPrimary,
                ),
            ) {
                Text(
                    text = "Keep editing",
                    style = TnyxTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun TabIcon(
    tab: ShellTab,
    highlighted: Boolean = false,
    muted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val outlineIconRes: Int? = when (tab) {
        ShellTab.Home -> R.drawable.ic_nav_home_outlined
        ShellTab.Nutrition -> R.drawable.ic_nav_nutrition_outlined
        ShellTab.MealPlan -> R.drawable.ic_nav_meal_plan_outlined
        ShellTab.Ai -> null
        ShellTab.Workout -> R.drawable.ic_nav_workout_outlined
        ShellTab.WorkoutLibrary -> R.drawable.ic_nav_library_outlined
        ShellTab.Progress -> R.drawable.ic_nav_progress_outlined
        ShellTab.You -> R.drawable.ic_user__outline
    }

    if (outlineIconRes != null) {
        Icon(
            painter = painterResource(id = outlineIconRes),
            contentDescription = null,
            tint = when {
                highlighted -> TnyxTheme.colors.warning
                muted -> TnyxTheme.colors.textMuted
                else -> TnyxTheme.colors.textSecondary
            },
            modifier = modifier,
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = when {
                highlighted -> TnyxTheme.colors.warning
                muted -> TnyxTheme.colors.textMuted
                else -> TnyxTheme.colors.ai
            },
            modifier = modifier,
        )
    }
}

private fun BottomNavigationMode.displayLabel(): String = when (this) {
    BottomNavigationMode.Workout -> "Workout"
    BottomNavigationMode.Nutrition -> "Nutrition"
    BottomNavigationMode.Hybrid -> "Hybrid"
    BottomNavigationMode.Custom -> "Custom"
}

private fun ShellTab.displayLabel(): String = when (this) {
    ShellTab.Home -> "Home"
    ShellTab.Nutrition -> "Nutrition"
    ShellTab.MealPlan -> "Meal Plan"
    ShellTab.Ai -> "Tio"
    ShellTab.Workout -> "Workout"
    ShellTab.WorkoutLibrary -> "Library"
    ShellTab.Progress -> "Progress"
    ShellTab.You -> "You"
}
