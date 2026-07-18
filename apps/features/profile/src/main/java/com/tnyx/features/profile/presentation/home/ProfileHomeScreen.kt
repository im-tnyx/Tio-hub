package com.tnyx.features.profile.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.avatar.TnyxAvatarSize
import com.tnyx.core.ui.components.avatar.TnyxUserAvatar
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()
    val headerHeight = 56.dp
    val showUsernameInHeader by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 1 ||
                (scrollState.firstVisibleItemIndex == 1 && scrollState.firstVisibleItemScrollOffset > 100)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 48.dp,
            ),
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(headerHeight))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceS)) {
                    UserProfileCard(
                        state = uiState,
                        onEditPhoto = { /* TODO: Action for edit photo */ },
                        onClick = { /* TODO: Action for card click */ },
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceSM))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceS)) {
                    ProgressPhotosBanner(
                        photoCount = uiState.progressPhotos.size,
                        lastPhotoUpdateWeight = uiState.lastPhotoUpdateWeight,
                        lastPhotoUpdateDate = uiState.lastPhotoUpdateDate,
                        onAddPictures = { onAction(ProfileHomeAction.AddProgressPhotosClicked) },
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceSM))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceS)) {
                    WorkoutWeeklyDurationChart(chart = uiState.workoutChart)
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceSM))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceS)) {
                    ActionsGrid2x2(onAction = onAction)
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceS)) {
                    WorkoutHistorySection()
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TnyxTheme.colors.surfaceRaised)
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .padding(horizontal = TnyxTheme.dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onAction(ProfileHomeAction.BackClicked) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
                Text(
                    text = if (showUsernameInHeader) uiState.displayName else "Profile",
                    style = TnyxTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                )
                IconButton(
                    onClick = { onAction(ProfileHomeAction.SettingsClicked) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    state: ProfileHomeUiState,
    onEditPhoto: () -> Unit,
    onClick: () -> Unit,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 0.dp,
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TnyxUserAvatar(
                    imageUrl = state.avatarUrl,
                    displayName = state.displayName,
                    membershipTier = state.membershipTier,
                    size = TnyxAvatarSize.Large,
                    onClick = onClick,
                    showEditBadge = true,
                    onEditClick = onEditPhoto,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.displayName,
                            style = TnyxTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary,
                        )
                        if (state.planLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Verified Premium",
                                tint = Color(0xFF1DA1F2),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.status,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(0.5.dp, TnyxTheme.colors.warning.copy(alpha = 0.4f), CircleShape)
                            .background(TnyxTheme.colors.warning.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = TnyxTheme.colors.warning,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.planLabel.uppercase(),
                                style = TnyxTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = TnyxTheme.colors.warning,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(start = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BMI: ",
                            style = TnyxTheme.typography.bodySmall,
                            color = TnyxTheme.colors.textSecondary,
                        )
                        Text(
                            text = if (state.bmi > 0.0) state.bmi.toString() else "-",
                            style = TnyxTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                    }

                    val (bmiStatus, bmiColor) = remember(state.bmi) {
                        when {
                            state.bmi <= 0.0 -> Pair("", Color.Transparent)
                            state.bmi < 18.5 -> Pair("Underweight", Color(0xFF03A9F4))
                            state.bmi < 25.0 -> Pair("Healthy", Color(0xFF4CAF50))
                            state.bmi < 30.0 -> Pair("Overweight", Color(0xFFFF9800))
                            else -> Pair("Obese", Color(0xFFF44336))
                        }
                    }

                    if (bmiStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bmiStatus,
                            style = TnyxTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = bmiColor,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MonitorWeight,
                        contentDescription = null,
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "WEIGHT",
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = TnyxTheme.colors.textMuted,
                        )
                        Text(
                            text = if (state.weight > 0.0) "${state.weight} kg" else "--",
                            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Straighten,
                        contentDescription = null,
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "HEIGHT",
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = TnyxTheme.colors.textMuted,
                        )
                        Text(
                            text = if (state.height > 0) "${state.height} cm" else "--",
                            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Whatshot,
                        contentDescription = null,
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BMR",
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = TnyxTheme.colors.textMuted,
                        )
                        Text(
                            text = if (state.bmr > 0) "${state.bmr} kcal" else "--",
                            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressPhotosBanner(
    photoCount: Int,
    lastPhotoUpdateWeight: String,
    lastPhotoUpdateDate: String,
    onAddPictures: () -> Unit,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 16.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier.size(width = 72.dp, height = 72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 50.dp, height = 62.dp)
                            .graphicsLayer { rotationZ = -12f }
                            .background(Color.White, RoundedCornerShape(3.dp))
                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(3.dp))
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color(0xFF2E2E2E)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 50.dp, height = 62.dp)
                            .graphicsLayer { rotationZ = 8f }
                            .background(Color.White, RoundedCornerShape(3.dp))
                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(3.dp))
                            .padding(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color(0xFF1C1C1C)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Picture",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (photoCount > 0) "$photoCount progress photos" else "No progress photos yet",
                        style = TnyxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            lastPhotoUpdateWeight.isNotBlank() && lastPhotoUpdateDate.isNotBlank() ->
                                "Last update: $lastPhotoUpdateWeight on $lastPhotoUpdateDate"
                            lastPhotoUpdateDate.isNotBlank() -> "Last update: $lastPhotoUpdateDate"
                            else -> "Add photos to track visual progress."
                        },
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                        lineHeight = 15.sp,
                    )
                }
            }
        }
    }
}

private data class WeeklyChartData(
    val headerValue: String,
    val points: List<Float>,
    val maxVal: Float,
    val axisLabels: List<String>,
)

private fun formatChartValue(selectedTab: Int, value: Float): String {
    return when (selectedTab) {
        1 -> "${value.toInt()} kg"
        2 -> "${value.toInt()} reps"
        else -> {
            val totalMinutes = value.toInt()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
    }
}

private fun weekCountForRange(range: String): Int {
    return when (range) {
        "3 Month" -> 13
        "6 Month" -> 26
        "12 Month" -> 52
        "All Time" -> 52
        else -> 7
    }
}

private fun valuesForSelectedTab(chart: WorkoutChartState, selectedTab: Int): List<Float> {
    return when (selectedTab) {
        1 -> chart.volumeKg
        2 -> chart.reps
        else -> chart.durationMinutes
    }
}

private fun axisLabelsForSelectedTab(selectedTab: Int, maxVal: Float): List<String> {
    val safeMax = maxVal.toInt().coerceAtLeast(1)
    val twoThirds = (safeMax * 2 / 3).coerceAtLeast(1)
    val oneThird = (safeMax / 3).coerceAtLeast(1)
    return when (selectedTab) {
        1 -> listOf("${safeMax}kg", "${twoThirds}kg", "${oneThird}kg", "0kg")
        2 -> listOf(safeMax.toString(), twoThirds.toString(), oneThird.toString(), "0")
        else -> listOf(
            formatChartValue(selectedTab, safeMax.toFloat()),
            formatChartValue(selectedTab, twoThirds.toFloat()),
            formatChartValue(selectedTab, oneThird.toFloat()),
            "0m",
        )
    }
}

private fun withThousandsSeparator(value: Int): String {
    return value.toString().reversed().chunked(3).joinToString(",").reversed()
}

private fun formatHeaderValue(selectedTab: Int, totalValue: Float): String {
    val total = totalValue.toInt()
    return when (selectedTab) {
        1 -> "${withThousandsSeparator(total)} kg"
        2 -> "$total reps"
        else -> {
            val hours = total / 60
            val minutes = total % 60
            "${hours}h ${minutes}m"
        }
    }
}

@Composable
private fun WorkoutWeeklyDurationChart(chart: WorkoutChartState) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 16.dp,
    ) {
        Column {
            val tabs = listOf("Duration", "Volume", "Reps")
            var selectedTab by remember { mutableStateOf(0) }
            var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
            val ranges = listOf("Weekly", "3 Month", "6 Month", "12 Month", "All Time")
            var selectedRange by remember { mutableStateOf(ranges.first()) }
            var isRangeMenuExpanded by remember { mutableStateOf(false) }
            val isWeeklyRange = selectedRange == "Weekly"

            val monday = remember {
                val today = LocalDate.now()
                today.minusDays((today.dayOfWeek.value - 1).toLong())
            }

            val periodStartDates = remember(monday, selectedRange) {
                if (isWeeklyRange) {
                    (0..6).map { monday.plusDays(it.toLong()) }
                } else {
                    val count = weekCountForRange(selectedRange)
                    (0 until count).map { index -> monday.minusWeeks((count - 1 - index).toLong()) }
                }
            }

            val pointDateLabels = remember(periodStartDates, isWeeklyRange) {
                if (isWeeklyRange) {
                    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")
                    periodStartDates.map { it.format(formatter) }
                } else {
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    periodStartDates.map { start ->
                        "${start.format(formatter)} - ${start.plusDays(6).format(formatter)}"
                    }
                }
            }

            val xAxisLabels = remember(periodStartDates, isWeeklyRange) {
                if (isWeeklyRange) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                } else {
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    val count = periodStartDates.size
                    val step = (count / 6).coerceAtLeast(1)
                    periodStartDates.mapIndexed { index, date ->
                        if (index % step == 0 || index == count - 1) date.format(formatter) else ""
                    }
                }
            }

            val weekRangeLabel = remember(monday) {
                val rangeFormatter = DateTimeFormatter.ofPattern("d MMM")
                "${monday.format(rangeFormatter)} - ${monday.plusDays(6).format(rangeFormatter)}"
            }

            val chartData = remember(selectedTab, chart) {
                val points = valuesForSelectedTab(chart, selectedTab)
                val maxVal = (points.maxOrNull() ?: 0f).coerceAtLeast(1f)
                WeeklyChartData(
                    headerValue = if (points.isEmpty()) "--" else formatHeaderValue(selectedTab, points.sum()),
                    points = points,
                    maxVal = maxVal,
                    axisLabels = axisLabelsForSelectedTab(selectedTab, maxVal),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chartData.headerValue,
                        style = TnyxTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedPointIndex?.let { idx ->
                            if (isWeeklyRange) weekRangeLabel else pointDateLabels.getOrNull(idx).orEmpty()
                        } ?: if (isWeeklyRange) {
                            "${tabs[selectedTab]} This Week"
                        } else {
                            "${tabs[selectedTab]} $selectedRange"
                        },
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Info",
                        tint = TnyxTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f))
                            .clickable { isRangeMenuExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedRange,
                                style = TnyxTheme.typography.labelMedium,
                                color = TnyxTheme.colors.textPrimary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = TnyxTheme.colors.textPrimary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isRangeMenuExpanded,
                        onDismissRequest = { isRangeMenuExpanded = false },
                        containerColor = TnyxTheme.colors.surfaceRaised,
                    ) {
                        ranges.forEach { range ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = range,
                                        style = TnyxTheme.typography.labelMedium,
                                        fontWeight = if (range == selectedRange) FontWeight.Bold else FontWeight.Normal,
                                        color = TnyxTheme.colors.textPrimary,
                                    )
                                },
                                onClick = {
                                    selectedRange = range
                                    selectedPointIndex = null
                                    isRangeMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (chartData.points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No workout data",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textMuted,
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .height(110.dp)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        chartData.axisLabels.forEach { label ->
                            Text(
                                text = label,
                                style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TnyxTheme.colors.textMuted,
                            )
                        }
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                    ) {
                        val chartHeightDp = maxHeight
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .pointerInput(chartData) {
                                    detectTapGestures { offset ->
                                        val lastIndex = chartData.points.size - 1
                                        val stepX = size.width / lastIndex.coerceAtLeast(1).toFloat()
                                        val tappedIndex = (offset.x / stepX).roundToInt().coerceIn(0, lastIndex)
                                        selectedPointIndex = if (selectedPointIndex == tappedIndex) null else tappedIndex
                                    }
                                },
                        ) {
                            val width = size.width
                            val height = size.height
                            val stepX = width / (chartData.points.size - 1).coerceAtLeast(1).toFloat()
                            val maxVal = chartData.maxVal

                            val gridLines = 3
                            for (i in 0..gridLines) {
                                val y = height * i / gridLines
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f,
                                )
                            }

                            val coords = chartData.points.mapIndexed { index, value ->
                                val x = index * stepX
                                val y = height - (value / maxVal) * (height - 20f) - 10f
                                Offset(x, y)
                            }

                            val fillPath = Path().apply {
                                moveTo(0f, height)
                                coords.forEach { offset -> lineTo(offset.x, offset.y) }
                                lineTo(width, height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent,
                                    ),
                                    startY = coords.minOf { it.y },
                                    endY = height,
                                ),
                            )

                            val linePath = Path().apply {
                                coords.forEachIndexed { index, offset ->
                                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = Color.White,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                            )

                            coords.forEach { offset ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.dp.toPx(),
                                    center = offset,
                                )
                                drawCircle(
                                    color = Color(0xFF1E1E1E),
                                    radius = 2.dp.toPx(),
                                    center = offset,
                                )
                            }

                            selectedPointIndex?.let { idx ->
                                val selectedX = coords[idx].x
                                drawLine(
                                    color = Color.White.copy(alpha = 0.3f),
                                    start = Offset(selectedX, 0f),
                                    end = Offset(selectedX, height),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f),
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = coords[idx],
                                )
                                drawCircle(
                                    color = Color(0xFF1E1E1E),
                                    radius = 3.dp.toPx(),
                                    center = coords[idx],
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            xAxisLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = TnyxTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TnyxTheme.colors.textMuted,
                                    maxLines = 1,
                                )
                            }
                        }

                        selectedPointIndex?.let { idx ->
                            val value = chartData.points[idx]
                            val yFraction = 1f - (value / chartData.maxVal)
                            val xFraction = idx / (chartData.points.size - 1).coerceAtLeast(1).toFloat()
                            val tooltipWidth = 76.dp
                            val tooltipX = (maxWidth * xFraction) - (tooltipWidth / 2)
                            val tooltipY = (chartHeightDp * yFraction) - 46.dp

                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = tooltipX.coerceIn(0.dp, (maxWidth - tooltipWidth).coerceAtLeast(0.dp)),
                                        y = tooltipY.coerceAtLeast(0.dp),
                                    )
                                    .width(tooltipWidth)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TnyxTheme.colors.textPrimary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = pointDateLabels.getOrNull(idx).orEmpty(),
                                        style = TnyxTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = TnyxTheme.colors.background,
                                    )
                                    Text(
                                        text = formatChartValue(selectedTab, value),
                                        style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TnyxTheme.colors.background,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.04f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedTab == index) {
                                    TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .clickable {
                                selectedTab = index
                                selectedPointIndex = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab,
                            style = TnyxTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                            ),
                            color = if (selectedTab == index) {
                                TnyxTheme.colors.textPrimary
                            } else {
                                TnyxTheme.colors.textMuted
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsGrid2x2(onAction: (ProfileHomeAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GridActionCard(
                painter = rememberVectorPainter(Icons.Rounded.BarChart),
                title = "Statistics",
                onClick = { /* Navigate stats */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            GridActionCard(
                painter = painterResource(R.drawable.ic_dumbbell),
                title = "Exercises",
                onClick = { /* Navigate exercises */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GridActionCard(
                painter = rememberVectorPainter(Icons.Rounded.Straighten),
                title = "Measures",
                onClick = { /* Navigate measures */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            GridActionCard(
                painter = rememberVectorPainter(Icons.Rounded.CalendarMonth),
                title = "Calendar",
                onClick = { /* Navigate calendar */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun GridActionCard(
    painter: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        onClick = onClick,
        padding = 16.dp,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = TnyxTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.KeyboardArrowRight),
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun WorkoutHistorySection() {
    Column {
        Text(
            text = "Workout History",
            style = TnyxTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(150.dp)
                    .offset(y = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TnyxTheme.colors.surfaceRaised.copy(alpha = 0.4f))
                    .border(
                        0.5.dp,
                        TnyxTheme.colors.textPrimary.copy(alpha = 0.04f),
                        RoundedCornerShape(16.dp),
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(130.dp)
                    .offset(y = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TnyxTheme.colors.surfaceRaised.copy(alpha = 0.7f))
                    .border(
                        0.5.dp,
                        TnyxTheme.colors.textPrimary.copy(alpha = 0.06f),
                        RoundedCornerShape(16.dp),
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TnyxTheme.colors.surfaceRaised)
                    .border(
                        0.5.dp,
                        TnyxTheme.colors.textPrimary.copy(alpha = 0.08f),
                        RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dumbbell),
                        contentDescription = null,
                        tint = TnyxTheme.colors.textMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No workouts",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textMuted,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Action to start tracking */ },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Start tracking here",
                style = TnyxTheme.typography.bodyLarge,
                color = TnyxTheme.colors.info,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = TnyxTheme.colors.info,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
