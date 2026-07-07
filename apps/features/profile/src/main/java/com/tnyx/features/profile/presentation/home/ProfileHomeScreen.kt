package com.tnyx.features.profile.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

@Composable
fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val headerHeight = 56.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
    ) {
        // --- 1. SCROLLABLE CONTENT ---
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = 48.dp
            )
        ) {
            // Spacer for Header + Status Bar
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(headerHeight))
            }

            // 1. User Profile Card
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    UserProfileCard(
                        state = uiState,
                        onEditPhoto = { /* TODO: Action for edit photo */ },
                        onClick = { /* TODO: Action for card click */ }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 2. Progress Photos Section (Redesigned visual)
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    ProgressPhotosBannerRedesign(
                        onAddPictures = { onAction(ProfileHomeAction.AddProgressPhotosClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 3. Weekly Workout Duration Chart
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    WorkoutWeeklyDurationChart()
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 4. 2x2 Action Items Grid
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    ActionsGrid2x2(onAction = onAction)
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 5. Workout History Section
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    WorkoutHistorySection()
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }
        }

        // --- 2. FIXED HEADER (Pins to top of Status Bar) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TnyxTheme.colors.background.copy(alpha = 0.95f))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .padding(horizontal = TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onAction(ProfileHomeAction.BackClicked) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TnyxTheme.colors.textPrimary
                    )
                }
                Text(
                    text = "Profile",
                    style = TnyxTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )
                IconButton(
                    onClick = { onAction(ProfileHomeAction.SupportClicked) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.HeadsetMic,
                        contentDescription = "Support",
                        tint = TnyxTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onAction(ProfileHomeAction.SettingsClicked) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = TnyxTheme.colors.textPrimary
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
    onClick: () -> Unit
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar with Overlay Pencil Edit icon
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(TnyxTheme.colors.surfaceVariant)
                            .border(1.dp, TnyxTheme.colors.primary.copy(alpha = 0.2f), CircleShape)
                            .clickable { onEditPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black, CircleShape)
                            .border(2.dp, TnyxTheme.colors.surfaceVariant.copy(alpha = 0.8f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name, Role, Premium Badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.displayName,
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary
                        )
                        if (state.planLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Verified Premium",
                                tint = Color(0xFF1DA1F2), // Twitter Blue color
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.status,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Premium Pill shape
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(0.5.dp, TnyxTheme.colors.warning.copy(alpha = 0.4f), CircleShape)
                            .background(TnyxTheme.colors.warning.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = TnyxTheme.colors.warning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.planLabel.uppercase(),
                                style = TnyxTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TnyxTheme.colors.warning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // BMI Badge Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .border(1.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BMI",
                        style = TnyxTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TnyxTheme.colors.textMuted
                    )
                    Text(
                        text = state.bmi.toString(),
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1B5E20).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Healthy",
                            style = TnyxTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weight
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonitorWeight,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "WEIGHT",
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = TnyxTheme.colors.textMuted
                        )
                        Text(
                            text = "${state.weight} kg",
                            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    thickness = 0.5.dp,
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
                )

                // Height
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Straighten,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "HEIGHT",
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = TnyxTheme.colors.textMuted
                        )
                        Text(
                            text = "${state.height} cm",
                            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    thickness = 0.5.dp,
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
                )

                // BMR
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Whatshot,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BMR",
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = TnyxTheme.colors.textMuted
                        )
                        Text(
                            text = "${state.bmr} kcal",
                            style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressPhotosBannerRedesign(
    onAddPictures: () -> Unit
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stacked tilted polaroids visual using Compose coordinates
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background polaroid 1 (rotated left)
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 64.dp)
                        .graphicsLayer { rotationZ = -12f }
                        .background(Color.White, RoundedCornerShape(3.dp))
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(3.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFF2E2E2E))
                    )
                }

                // Foreground polaroid 2 (rotated right)
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 64.dp)
                        .graphicsLayer { rotationZ = 8f }
                        .background(Color.White, RoundedCornerShape(3.dp))
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(3.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFF1C1C1C))
                    )
                }

                // Green overlay "+" button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C853)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Picture",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main text instructions & CTA
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "What you see is what you believe!",
                    style = TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Consistently upload your photos to observe the changes over time.",
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add Pictures pill button
            OutlinedButton(
                onClick = onAddPictures,
                border = BorderStroke(1.dp, Color(0xFF00C853)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Add Pictures",
                        style = TnyxTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutWeeklyDurationChart() {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 16.dp
    ) {
        Column {
            // Header: This Week (i) & Dropdown Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "This Week",
                        style = TnyxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Info",
                        tint = TnyxTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Dropdown Pill Option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Weekly",
                            style = TnyxTheme.typography.labelSmall,
                            color = TnyxTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = TnyxTheme.colors.textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Durations text
            Text(
                text = "3h 18m",
                style = TnyxTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Text(
                text = "Total Duration",
                style = TnyxTheme.typography.labelSmall,
                color = TnyxTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Chart
            val points = listOf(15f, 35f, 15f, 15f, 75f, 15f, 65f) // Mon to Sun mock heights
            val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / 6f
                    val maxVal = 90f // scale

                    // Draw grid lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = height * i / gridLines
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    val coords = points.mapIndexed { index, value ->
                        val x = index * stepX
                        val y = height - (value / maxVal) * (height - 20f) - 10f
                        Offset(x, y)
                    }

                    // Brush gradient area fill under the curve
                    val fillPath = Path().apply {
                        moveTo(0f, height)
                        coords.forEach { offset ->
                            lineTo(offset.x, offset.y)
                        }
                        lineTo(width, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            startY = coords.minOf { it.y },
                            endY = height
                        )
                    )

                    // Draw connection path
                    val linePath = Path().apply {
                        coords.forEachIndexed { index, offset ->
                            if (index == 0) moveTo(offset.x, offset.y)
                            else lineTo(offset.x, offset.y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = Color.White,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw circles
                    coords.forEach { offset ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = Color(0xFF1E1E1E), // background dark circle center
                            radius = 2.dp.toPx(),
                            center = offset
                        )
                    }
                }

                // X Axis labels aligned under data points
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            style = TnyxTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TnyxTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // In-chart Tab Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.04f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf("Duration", "Volume", "Reps")
                var selectedTab by remember { mutableStateOf(0) }
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == index) TnyxTheme.colors.textPrimary.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = TnyxTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ),
                            color = if (selectedTab == index) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textMuted
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridActionCard(
                icon = Icons.Rounded.BarChart,
                title = "Statistics",
                subtitle = "Detailed insights",
                onClick = { /* Navigate stats */ },
                modifier = Modifier.weight(1f)
            )
            GridActionCard(
                icon = Icons.Rounded.FitnessCenter,
                title = "Exercises",
                subtitle = "Your exercise library",
                onClick = { /* Navigate exercises */ },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridActionCard(
                icon = Icons.Rounded.Straighten,
                title = "Measures",
                subtitle = "Body & performance",
                onClick = { /* Navigate measures */ },
                modifier = Modifier.weight(1f)
            )
            GridActionCard(
                icon = Icons.Rounded.CalendarMonth,
                title = "Calendar",
                subtitle = "Workout schedule",
                onClick = { /* Navigate calendar */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GridActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        onClick = onClick,
        padding = 12.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = TnyxTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TnyxTheme.colors.textMuted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(16.dp)
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
            color = TnyxTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Empty state card
        TnyxCard(
            variant = TnyxCardVariant.Normal,
            padding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No workouts",
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom blue tracker button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Action to start tracking */ },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start tracking here",
                style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = TnyxTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
