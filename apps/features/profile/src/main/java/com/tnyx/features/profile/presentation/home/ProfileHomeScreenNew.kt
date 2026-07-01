package com.tnyx.features.profile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader

@Composable
fun ProfileHomeScreenNew(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = 120.dp
            )
        ) {
            // Status bar + header space
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(48.dp))
            }

            // 1. User Profile Card
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    UserProfileCardNew(uiState = uiState)
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 2. Stats Grid (2x2)
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    StatsGrid(
                        workoutHistory = uiState.workoutHistory,
                        calendar = uiState.calendarStats,
                        statistics = uiState.overallStats,
                        exercises = uiState.exerciseLibrary,
                        onWorkoutHistoryClick = { onAction(ProfileHomeAction.ViewWorkoutHistoryClicked) },
                        onCalendarClick = { onAction(ProfileHomeAction.ViewCalendarClicked) },
                        onStatisticsClick = { onAction(ProfileHomeAction.ViewStatisticsClicked) },
                        onExercisesClick = { onAction(ProfileHomeAction.ViewExercisesClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 3. Current Journey (simplified placeholder)
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    CurrentJourneyCard(
                        journey = uiState.currentJourney,
                        onHistoryClick = { onAction(ProfileHomeAction.JourneyHistoryClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 4. Progress Photos (simplified placeholder)
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    ProgressPhotosCard(
                        onAddClick = { onAction(ProfileHomeAction.AddProgressPhotosClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 5. Quick Actions
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    QuickActionsSection(onAction = onAction)
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 6. More
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    MoreSection(onAction = onAction)
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 7. Health Connections
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    HealthConnectionsSection(
                        onClick = { onAction(ProfileHomeAction.HealthConnectionsClicked) }
                    )
                }
            }
        }

        // Fixed Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TnyxTheme.colors.background)
                .statusBarsPadding()
        ) {
            TnyxScreenHeader(
                title = "Profile",
                alpha = 1f,
                height = 48.dp,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigationClick = { onAction(ProfileHomeAction.BackClicked) },
                actions = {
                    IconButton(onClick = { onAction(ProfileHomeAction.SupportClicked) }) {
                        Icon(
                            imageVector = Icons.Rounded.HeadsetMic,
                            contentDescription = "Support",
                            tint = TnyxTheme.colors.textPrimary
                        )
                    }
                    IconButton(onClick = { onAction(ProfileHomeAction.SettingsClicked) }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = TnyxTheme.colors.textPrimary
                        )
                    }
                }
            )
        }
    }
}

// === USER PROFILE CARD ===
@Composable
private fun UserProfileCardNew(
    uiState: ProfileHomeUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TnyxTheme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TnyxTheme.dimens.SpaceM)
        ) {
            // Top Row: Avatar + Name + BMI + Health Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Avatar + Name + Plan
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TnyxTheme.colors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = TnyxTheme.colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Name + Plan
                    Column {
                        Text(
                            text = uiState.displayName,
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary
                        )
                        Text(
                            text = uiState.planLabel,
                            style = TnyxTheme.typography.labelMedium,
                            color = TnyxTheme.colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Right: BMI + Health Status
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "BMI",
                        style = TnyxTheme.typography.labelSmall,
                        color = TnyxTheme.colors.textSecondary
                    )
                    Text(
                        text = "%.1f".format(uiState.bmi),
                        style = TnyxTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Health Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (uiState.bmiStatus.isHealthy) 
                                TnyxTheme.colors.success 
                            else 
                                TnyxTheme.colors.warning
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.bmiStatus.label,
                            style = TnyxTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.bmiStatus.isHealthy) 
                                TnyxTheme.colors.success 
                            else 
                                TnyxTheme.colors.warning
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = TnyxTheme.colors.textSecondary.copy(alpha = 0.12f).copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Row: Weight, Height, BMR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Rounded.FitnessCenter,
                    label = "WEIGHT",
                    value = "${uiState.weight} kg",
                    iconTint = TnyxTheme.colors.primary
                )
                StatItem(
                    icon = Icons.Rounded.Height,
                    label = "HEIGHT",
                    value = "${uiState.height} cm",
                    iconTint = TnyxTheme.colors.info
                )
                StatItem(
                    icon = Icons.Rounded.LocalFireDepartment,
                    label = "BMR",
                    value = "${uiState.bmr} kcal",
                    iconTint = TnyxTheme.colors.error
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = TnyxTheme.colors.textSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = TnyxTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
    }
}

// === STATS GRID 2x2 ===
@Composable
private fun StatsGrid(
    workoutHistory: WorkoutHistoryStats,
    calendar: CalendarStats,
    statistics: OverallStats,
    exercises: ExerciseLibraryStats,
    onWorkoutHistoryClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onExercisesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Row 1: Workout History + Calendar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.FitnessCenter,
                title = "Workout History",
                line1 = "${workoutHistory.weeklyCount} workouts",
                line2 = "${workoutHistory.currentStreak}-day streak",
                iconTint = TnyxTheme.colors.primary,
                onClick = onWorkoutHistoryClick
            )
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.CalendarToday,
                title = "Calendar",
                line1 = calendar.nextWorkout ?: "No workout",
                line2 = calendar.nextDate ?: "Not scheduled",
                iconTint = TnyxTheme.colors.info,
                onClick = onCalendarClick
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Row 2: Statistics + Exercises
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.BarChart,
                title = "Statistics",
                line1 = "${statistics.totalWorkouts} workouts",
                line2 = "${statistics.totalCalories / 1000}k kcal burned",
                iconTint = TnyxTheme.colors.success,
                onClick = onStatisticsClick
            )
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Accessibility,
                title = "Exercises",
                line1 = "${exercises.totalExercises} exercises",
                line2 = "${exercises.favoriteCount} favorites",
                iconTint = TnyxTheme.colors.warning,
                onClick = onExercisesClick
            )
        }
    }
}

@Composable
private fun StatsCard(
    icon: ImageVector,
    title: String,
    line1: String,
    line2: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TnyxTheme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TnyxTheme.colors.textSecondary
                )
            }
            Column {
                Text(
                    text = title,
                    style = TnyxTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TnyxTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line1,
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
                Text(
                    text = line2,
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
            }
        }
    }
}

// === CURRENT JOURNEY (Simplified Placeholder) ===
@Composable
private fun CurrentJourneyCard(
    journey: CurrentJourneyState,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TnyxTheme.colors.surface
        )
    ) {
        Column(modifier = Modifier.padding(TnyxTheme.dimens.SpaceM)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Journey",
                    style = TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                TextButton(onClick = onHistoryClick) {
                    Text("History")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = journey.name,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { journey.progress },
                modifier = Modifier.fillMaxWidth(),
                color = TnyxTheme.colors.primary,
                trackColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.12f).copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${journey.initialWeight} kg",
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
                Text(
                    text = "${(journey.progress * 100).toInt()}% to goal",
                    style = TnyxTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TnyxTheme.colors.primary
                )
                Text(
                    text = "${journey.targetWeight} kg",
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
            }
        }
    }
}

// === PROGRESS PHOTOS (Simplified Placeholder) ===
@Composable
private fun ProgressPhotosCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TnyxTheme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(TnyxTheme.dimens.SpaceM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TnyxTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "What you see is what you believe!",
                style = TnyxTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Consistently upload your photos to observe the changes over time.",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddClick) {
                Text("Add Pictures")
            }
        }
    }
}

// === QUICK ACTIONS ===
@Composable
private fun QuickActionsSection(
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Quick Actions",
            style = TnyxTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionItem(
                icon = Icons.Rounded.Restaurant,
                title = "Nutrition Targets",
                subtitle = "Set and track your nutrition goals",
                iconTint = TnyxTheme.colors.success,
                onClick = { onAction(ProfileHomeAction.NutritionTargetsClicked) }
            )
            QuickActionItem(
                icon = Icons.Rounded.FitnessCenter,
                title = "Workout Settings",
                subtitle = "Customize your workout preferences",
                iconTint = TnyxTheme.colors.info,
                onClick = { onAction(ProfileHomeAction.WorkoutSettingsClicked) }
            )
            QuickActionItem(
                icon = Icons.Rounded.BarChart,
                title = "Graph Settings",
                subtitle = "Personalize your progress graphs",
                iconTint = TnyxTheme.colors.primary,
                onClick = { onAction(ProfileHomeAction.GraphSettingsClicked) }
            )
            QuickActionItem(
                icon = Icons.Outlined.DevicesOther,
                title = "Wear OS Settings",
                subtitle = "Configure your Wear OS experience",
                iconTint = TnyxTheme.colors.warning,
                onClick = { onAction(ProfileHomeAction.WearOsSettingsClicked) }
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TnyxTheme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TnyxTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TnyxTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TnyxTheme.colors.textSecondary
            )
        }
    }
}

// === MORE SECTION ===
@Composable
private fun MoreSection(
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "More",
            style = TnyxTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionItem(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                title = "Resources",
                subtitle = "Articles, guides and more",
                iconTint = TnyxTheme.colors.info,
                onClick = { onAction(ProfileHomeAction.ResourcesClicked) }
            )
            QuickActionItem(
                icon = Icons.Rounded.CardGiftcard,
                title = "Rewards",
                subtitle = "Earn and redeem rewards",
                iconTint = TnyxTheme.colors.warning,
                onClick = { onAction(ProfileHomeAction.RewardsClicked) }
            )
        }
    }
}

// === HEALTH CONNECTIONS ===
@Composable
private fun HealthConnectionsSection(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "HEALTH APP CONNECTIONS",
                style = TnyxTheme.typography.labelMedium,
                color = TnyxTheme.colors.textMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.width(8.dp))
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 0.5.dp,
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = TnyxTheme.colors.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Watch,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TnyxTheme.colors.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connect your wearables",
                        style = TnyxTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sync steps, workouts, sleep, and recovery from your favorite devices.",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Manage connections")
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
