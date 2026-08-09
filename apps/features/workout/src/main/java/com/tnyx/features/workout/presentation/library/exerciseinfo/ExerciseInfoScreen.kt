package com.tnyx.features.workout.presentation.library.exerciseinfo

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.shared.workout.domain.model.ExerciseDefinition
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import okhttp3.OkHttpClient

@Composable
fun ExerciseInfoScreen(
    state: ExerciseInfoUiState,
    onAction: (ExerciseInfoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val availableTabs = remember(state.exercise?.instructions) {
        buildList {
            add(ExerciseInfoTab.SUMMARY)
            add(ExerciseInfoTab.HISTORY)
            add(ExerciseInfoTab.HOW_TO)
        }
    }
    val resolvedSelectedTab = availableTabs.firstOrNull { tab -> tab == state.selectedTab }
        ?: ExerciseInfoTab.SUMMARY

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding(),
    ) {
        // Top App Bar: Back Arrow | Title | Share Icon | More Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { onAction(ExerciseInfoAction.BackClicked) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TnyxTheme.colors.textPrimary,
                )
            }

            Text(
                text = state.exercise?.name ?: "Exercise",
                style = TnyxTheme.typography.titleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )

            Row {
                IconButton(onClick = { /* Share */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "More",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            }
        }

        // Top Navigation Tab Bar
        ExerciseInfoTabBar(
            selectedTab = resolvedSelectedTab,
            tabs = availableTabs,
            onTabSelected = { onAction(ExerciseInfoAction.TabSelected(it)) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TnyxTheme.colors.accent)
                    }
                }

                state.exercise == null -> {
                    EmptyExerciseInfoState(
                        message = state.errorMessage ?: "Exercise details unavailable.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    ExerciseInfoContent(
                        exercise = state.exercise,
                        mediaVariant = state.mediaVariant,
                        selectedTab = resolvedSelectedTab,
                        isVideoPlaying = state.isVideoPlaying,
                        onTogglePlayback = { onAction(ExerciseInfoAction.VideoPlaybackToggled) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseInfoContent(
    exercise: ExerciseDefinition,
    mediaVariant: ExerciseMediaVariant?,
    selectedTab: ExerciseInfoTab,
    isVideoPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = TnyxDimens.SpaceXL),
    ) {
        // Exercise Media Hero Image / Video Header (Pure White Background)
        item {
            ExerciseMediaHero(
                exercise = exercise,
                mediaVariant = mediaVariant,
                isVideoPlaying = isVideoPlaying,
                onTogglePlayback = onTogglePlayback,
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            when (selectedTab) {
                ExerciseInfoTab.SUMMARY -> ExerciseSummaryTab(exercise = exercise)
                ExerciseInfoTab.HISTORY -> ExerciseHistoryTab(exercise = exercise)
                ExerciseInfoTab.HOW_TO -> ExerciseHowToTab(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseMediaHero(
    exercise: ExerciseDefinition,
    mediaVariant: ExerciseMediaVariant?,
    isVideoPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mediaAsset = remember(exercise, mediaVariant) {
        resolveExerciseInfoMediaAsset(
            exercise = exercise,
            mediaVariant = mediaVariant,
        )
    }
    val videoUrl = mediaAsset?.videoRef?.takeIf { it.isNotBlank() }
    val imageUrl = mediaAsset?.imageRef ?: mediaAsset?.thumbnailRef ?: mediaAsset?.videoRef
    var hasPlaybackError by remember(videoUrl) { mutableStateOf(false) }
    var player by remember(videoUrl) { mutableStateOf<ExoPlayer?>(null) }
    val httpClient = remember(context) {
        OkHttpClient.Builder()
            .cache(okhttp3.Cache(java.io.File(context.cacheDir, "exercise_media_cache"), 100 * 1024 * 1024L))
            .build()
    }

    LaunchedEffect(context, videoUrl, httpClient) {
        player?.release()
        player = null
        hasPlaybackError = false

        val resolvedVideoUrl = videoUrl ?: return@LaunchedEffect
        player = runCatching {
            val upstreamFactory = OkHttpDataSource.Factory(httpClient)
                .setUserAgent(context.packageName)
            val dataSourceFactory = DefaultDataSource.Factory(context, upstreamFactory)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    setMediaItem(MediaItem.fromUri(Uri.parse(resolvedVideoUrl)))
                    prepare()
                }
        }.getOrElse {
            hasPlaybackError = true
            null
        }
    }

    DisposableEffect(player) {
        val currentPlayer = player
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                hasPlaybackError = true
            }
        }
        currentPlayer?.addListener(listener)
        onDispose {
            currentPlayer?.removeListener(listener)
            currentPlayer?.stop()
            currentPlayer?.clearMediaItems()
            currentPlayer?.release()
            player = null
        }
    }

    LaunchedEffect(player, isVideoPlaying, hasPlaybackError) {
        val currentPlayer = player ?: return@LaunchedEffect
        if (hasPlaybackError) return@LaunchedEffect
        currentPlayer.playWhenReady = isVideoPlaying
        if (isVideoPlaying) {
            currentPlayer.play()
        } else {
            currentPlayer.pause()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(TnyxTheme.colors.surfaceRaised),
        contentAlignment = Alignment.Center,
    ) {
        // 1. Instant Poster Overlay (Renders in 0ms from disk cache while video loads)
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = exercise.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 2. ExoPlayer View (Overlays poster seamlessly once video frame is ready)
        val currentPlayer = player
        if (currentPlayer != null && !hasPlaybackError) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                update = { view ->
                    view.player = currentPlayer
                },
                onRelease = { view ->
                    view.player = null
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceSM),
            ) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textMuted,
                    modifier = Modifier.size(TnyxDimens.IconXL),
                )
            }
        }

        // Top Right Pause / Play Overlay Button
        if (videoUrl != null && !hasPlaybackError) {
            IconButton(
                onClick = onTogglePlayback,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.45f)),
            ) {
                Icon(
                    imageVector = if (isVideoPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isVideoPlaying) "Pause video" else "Play video",
                    tint = TnyxTheme.colors.background,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ExerciseSummaryTab(
    exercise: ExerciseDefinition,
    modifier: Modifier = Modifier,
) {
    var selectedMetric by rememberSaveable { mutableStateOf(SummaryMetric.HeaviestWeight) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ExerciseTitleBlock(
            exercise = exercise,
            showMetaLines = true,
        )

        ExerciseLoggingHint(
            exercise = exercise,
            modifier = Modifier.fillMaxWidth(),
        )

        ExerciseSummaryChartCard(modifier = Modifier.fillMaxWidth())

        SummaryMetricSelector(
            selectedMetric = selectedMetric,
            onMetricSelected = { selectedMetric = it },
        )

        ExercisePersonalRecordsSection(modifier = Modifier.fillMaxWidth())

        ExerciseStrengthLevelSection(modifier = Modifier.fillMaxWidth())

        ExerciseSetRecordsSection(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ExerciseHistoryTab(
    exercise: ExerciseDefinition,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ExerciseTitleBlock(
            exercise = exercise,
            showMetaLines = true,
        )

        TnyxCard(
            variant = TnyxCardVariant.Surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textSecondary,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = "No workout history yet",
                    style = TnyxTheme.typography.titleMedium.copy(
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Jab is exercise ke workout logs available honge tab yahan recent sessions aur trend history dikh jayegi.",
                    style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textSecondary),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ExerciseHowToTab(
    exercise: ExerciseDefinition,
    modifier: Modifier = Modifier,
) {
    val steps = exercise.instructions.filter { instruction -> instruction.isNotBlank() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ExerciseTitleBlock(
            exercise = exercise,
            showMetaLines = false,
        )

        if (steps.isEmpty()) {
            EmptyExerciseInfoState(
                message = "How to steps unavailable.",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = TnyxTheme.typography.bodyLarge.copy(
                                color = TnyxTheme.colors.textMuted,
                                fontWeight = FontWeight.Normal,
                            ),
                            modifier = Modifier.width(24.dp),
                        )
                        Text(
                            text = step.removePrefix("${index + 1}.").trim(),
                            style = TnyxTheme.typography.bodyLarge.copy(
                                color = TnyxTheme.colors.textPrimary,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 22.sp,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseTitleBlock(
    exercise: ExerciseDefinition,
    showMetaLines: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = exercise.name,
            style = TnyxTheme.typography.headlineMedium.copy(
                color = TnyxTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
        )
        if (showMetaLines) {
            val primaryMuscle = exercise.primaryMuscleGroups.firstOrNull()?.toDisplayLabel()
                ?: exercise.bodyPart?.toDisplayLabel()
                ?: "Not available"
            val secondaryMuscles = exercise.secondaryMuscleGroups.toDisplayList()

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Primary:",
                    style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textMuted),
                )
                Text(
                    text = primaryMuscle,
                    style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textSecondary),
                )
            }

            if (secondaryMuscles.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Secondary:",
                        style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textMuted),
                    )
                    Text(
                        text = secondaryMuscles,
                        style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textSecondary),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseLoggingHint(
    exercise: ExerciseDefinition,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Lightbulb,
            contentDescription = null,
            tint = TnyxTheme.colors.accent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = buildLoggingHint(exercise),
            style = TnyxTheme.typography.titleMedium.copy(
                color = TnyxTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            ),
        )
    }
}

@Composable
private fun ExerciseSummaryChartCard(
    modifier: Modifier = Modifier,
) {
    TnyxCard(
        variant = TnyxCardVariant.Surface,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textMuted,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = "No data yet",
                    style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textMuted),
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricSelector(
    selectedMetric: SummaryMetric,
    onMetricSelected: (SummaryMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(SummaryMetric.entries) { metric ->
            val isSelected = metric == selectedMetric
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.surfaceVariant
                    )
                    .clickable { onMetricSelected(metric) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = metric.label,
                    style = TnyxTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) TnyxTheme.colors.background else TnyxTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ExercisePersonalRecordsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "🏆", fontSize = 16.sp)
            SectionTitle(title = "Personal Records", modifier = Modifier.weight(1f))
            Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help",
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        TnyxCard(
            variant = TnyxCardVariant.Surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                RecordRow(label = "Heaviest Weight", value = "-")
                RecordRow(label = "Best 1RM", value = "-")
                RecordRow(label = "Best Set Volume", value = "-")
                RecordRow(label = "Best Session Volume", value = "-", showDivider = false)
            }
        }
    }
}

@Composable
private fun ExerciseStrengthLevelSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionTitle(title = "Strength Level", modifier = Modifier.weight(1f))
            Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help",
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        TnyxCard(
            variant = TnyxCardVariant.Surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Best 1RM",
                            style = TnyxTheme.typography.bodySmall.copy(color = TnyxTheme.colors.textMuted)
                        )
                        Text(
                            text = "-",
                            style = TnyxTheme.typography.titleMedium.copy(
                                color = TnyxTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Level",
                            style = TnyxTheme.typography.bodySmall.copy(color = TnyxTheme.colors.textMuted)
                        )
                        Text(
                            text = "-",
                            style = TnyxTheme.typography.titleMedium.copy(
                                color = TnyxTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // 4-Segment Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TnyxTheme.colors.surfaceVariant)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Beginner", style = TnyxTheme.typography.labelSmall, color = TnyxTheme.colors.textMuted)
                    Text(text = "Intermediate", style = TnyxTheme.typography.labelSmall, color = TnyxTheme.colors.textMuted)
                    Text(text = "Advanced", style = TnyxTheme.typography.labelSmall, color = TnyxTheme.colors.textMuted)
                    Text(text = "Elite", style = TnyxTheme.typography.labelSmall, color = TnyxTheme.colors.textMuted)
                }
            }
        }
    }
}

@Composable
private fun ExerciseSetRecordsSection(
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionTitle(title = "Set Records", modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = "Expand/Collapse",
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        if (expanded) {
            TnyxCard(
                variant = TnyxCardVariant.Surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Reps",
                            style = TnyxTheme.typography.titleSmall.copy(
                                color = TnyxTheme.colors.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            text = "Personal Best",
                            style = TnyxTheme.typography.titleSmall.copy(
                                color = TnyxTheme.colors.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No set records yet",
                            style = TnyxTheme.typography.bodyMedium.copy(color = TnyxTheme.colors.textMuted),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = TnyxTheme.typography.titleMedium.copy(
            color = TnyxTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        ),
        modifier = modifier,
    )
}

@Composable
private fun RecordRow(
    label: String,
    value: String,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyLarge.copy(color = TnyxTheme.colors.textPrimary),
            )
            Text(
                text = value,
                style = TnyxTheme.typography.bodyLarge.copy(
                    color = if (value != "-") TnyxTheme.colors.accent else TnyxTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)),
            )
        }
    }
}

@Composable
private fun ExerciseInfoTabBar(
    selectedTab: ExerciseInfoTab,
    tabs: List<ExerciseInfoTab>,
    onTabSelected: (ExerciseInfoTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = tab.label,
                        style = TnyxTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            color = if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                TnyxTheme.colors.accent.copy(alpha = if (isSelected) 1f else 0f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyExerciseInfoState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = TnyxTheme.typography.bodyLarge.copy(color = TnyxTheme.colors.textSecondary),
            textAlign = TextAlign.Center,
        )
    }
}

private enum class SummaryMetric(val label: String) {
    HeaviestWeight("Heaviest Weight"),
    OneRepMax("One Rep Max"),
    BestSetVolume("Best Set Volume"),
    BestSessionVolume("Best Session Volume"),
}

private val ExerciseInfoTab.label: String
    get() = when (this) {
        ExerciseInfoTab.SUMMARY -> "Summary"
        ExerciseInfoTab.HISTORY -> "History"
        ExerciseInfoTab.HOW_TO -> "How to"
    }

private fun buildLoggingHint(exercise: ExerciseDefinition): String {
    val equipmentLabel = exercise.equipment.firstOrNull()?.toDisplayLabel()?.lowercase()
    return if (equipmentLabel.isNullOrBlank()) {
        "How to log this exercise"
    } else {
        "How to log $equipmentLabel exercises"
    }
}

private fun List<String>.toDisplayList(): String {
    return joinToString(", ") { value -> value.toDisplayLabel() }
}

private fun String.toDisplayLabel(): String {
    return replace("_", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}
