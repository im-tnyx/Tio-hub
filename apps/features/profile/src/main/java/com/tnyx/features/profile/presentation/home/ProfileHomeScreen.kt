package com.tnyx.features.profile.presentation.home

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.profile.R

@Composable
fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()

    // Header dimension
    val headerHeight = 48.dp

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
                bottom = 120.dp
            )
        ) {
            // Reserved space for Header + Status Bar
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(headerHeight))
            }
            // ... rest of lazy column ...

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

            // 2. Current Journey Section
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    ProfileJourneyCard(
                        state = uiState,
                        onHistoryClick = { onAction(ProfileHomeAction.JourneyHistoryClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 3. Progress Photos Section
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    ProgressPhotosBanner(
                        state = uiState,
                        onAddPictures = { onAction(ProfileHomeAction.AddProgressPhotosClicked) }
                    )
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 4. Quick Actions Section
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    Column {
                        SectionHeader(title = "Quick Actions")
                        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
                            LauncherItem(
                                icon = Icons.Rounded.Restaurant,
                                iconTint = TnyxTheme.colors.success,
                                title = "Nutrition Targets",
                                subtitle = "Calories, macros, water & more",
                                onClick = { onAction(ProfileHomeAction.NutritionTargetsClicked) }
                            )
                            LauncherItem(
                                icon = Icons.Rounded.Accessibility,
                                iconTint = TnyxTheme.colors.warning,
                                title = "Workout Settings",
                                subtitle = "Rest timer, warm-up, plates & more",
                                onClick = { onAction(ProfileHomeAction.WorkoutSettingsClicked) }
                            )
                            LauncherItem(
                                icon = Icons.Rounded.BarChart,
                                iconTint = TnyxTheme.colors.info,
                                title = "Graph Settings",
                                subtitle = "Customize your progress graphs",
                                onClick = { onAction(ProfileHomeAction.GraphSettingsClicked) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 5. More Section
            item {
                Box(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
                    Column {
                        SectionHeader(title = "More")
                        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                        Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS)) {
                            LauncherItem(
                                icon = Icons.Rounded.CardGiftcard,
                                iconTint = TnyxTheme.colors.warning,
                                title = "Rewards",
                                subtitle = "Refer friends, earn rewards",
                                onClick = { onAction(ProfileHomeAction.RewardsClicked) }
                            )
                            LauncherItem(
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                iconTint = TnyxTheme.colors.info,
                                title = "Resources",
                                subtitle = "Guides, articles & tools",
                                onClick = { onAction(ProfileHomeAction.ResourcesClicked) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            }

            // 6. Health App Connections Section
            item {
                Box(modifier = Modifier
                    .padding(horizontal = TnyxTheme.dimens.SpaceM)
                    .navigationBarsPadding()) {
                    Column {
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
                        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                        HealthConnectionsCard(
                            onManageClick = { onAction(ProfileHomeAction.HealthConnectionsClicked) }
                        )
                    }
                }
            }
        }

        // --- 2. FIXED HEADER (Pins to top of Status Bar) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TnyxTheme.colors.background)
                .statusBarsPadding()
        ) {
            TnyxScreenHeader(
                title = "Profile",
                alpha = 1f,
                height = headerHeight,
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

@Composable
private fun ProfileJourneyCard(
    state: ProfileHomeUiState,
    onHistoryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Journey",
                style = TnyxTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Row(
                modifier = Modifier.clickable { onHistoryClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = TnyxTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TnyxTheme.colors.textPrimary
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        // Subtitle
        Text(
            text = buildAnnotatedString {
                append("Check out your progress on ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TnyxTheme.colors.textPrimary)) {
                    append(state.currentJourney.name)
                }
                append(" journey")
            },
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary
        )
        Spacer(Modifier.height(16.dp))
        // The Card
        TnyxCard(variant = TnyxCardVariant.Normal, padding = 16.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeightLabel(label = "Initial Weight", value = "${state.currentJourney.initialWeight} kg")
                    WeightLabel(label = "Goal", value = "${state.currentJourney.targetWeight} kg", alignEnd = true)
                }
                Spacer(Modifier.height(24.dp))
                // Visual Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    // Track Line
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp, end = 8.dp)
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
                    )
                    // Trophy
                    Image(
                        painter = painterResource(id = R.drawable.trophy),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp)
                            .size(44.dp),
                        contentScale = ContentScale.Fit
                    )
                    // Walking Person
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                    ) {
                        val trackWidth = this.maxWidth
                        val effectiveWidth = trackWidth - 10.dp
                        val leftOffset = effectiveWidth * state.currentJourney.progress
                        
                        Image(
                            painter = painterResource(id = R.drawable.journey),
                            contentDescription = null,
                            modifier = Modifier
                                .offset(x = leftOffset)
                                .size(60.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightLabel(label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = label,
            style = TnyxTheme.typography.labelMedium,
            color = TnyxTheme.colors.textSecondary
        )
        Text(
            text = value,
            style = TnyxTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
    }
}

@Composable
private fun ProgressPhotosBanner(
    state: ProfileHomeUiState,
    onAddPictures: () -> Unit
) {
    if (state.progressPhotos.isEmpty()) {
        ProgressPhotosEmptyState(onAddPictures = onAddPictures)
    } else {
        ProgressPhotosPopulatedState(state = state, onAddPhoto = onAddPictures)
    }
}

@Composable
private fun ProgressPhotosEmptyState(onAddPictures: () -> Unit) {
    TnyxCard(variant = TnyxCardVariant.Normal, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.mirror),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "What you see is what you believe!",
                    style = TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Consistently upload your photos to observe the changes over time.",
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.textSecondary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Add Pictures",
                    style = TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onAddPictures() }
                )
            }
        }
    }
}

@Composable
private fun ProgressPhotosPopulatedState(
    state: ProfileHomeUiState,
    onAddPhoto: () -> Unit
) {
    TnyxCard(variant = TnyxCardVariant.Normal, padding = 16.dp) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Consistently upload your photos to observe the changes over time.",
                style = TnyxTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Spacer(Modifier.height(24.dp))
            // Photo List
            Box(modifier = Modifier.height(60.dp).fillMaxWidth()) {
                state.progressPhotos.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .offset(x = (index * 45).dp)
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, TnyxTheme.colors.surface, CircleShape)
                            .background(TnyxTheme.colors.surfaceVariant)
                    )
                }
                // Add Photo Button
                Box(
                    modifier = Modifier
                        .offset(x = (state.progressPhotos.size * 45).dp)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(TnyxTheme.colors.primary)
                        .border(2.dp, TnyxTheme.colors.surface, CircleShape)
                        .clickable { onAddPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CameraAlt, null, tint = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
            // Dashed Divider (Simulated)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(20) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)))
                }
            }
            Spacer(Modifier.height(16.dp))
            // Last Update Info
            Row {
                Text(
                    text = "Last Update: ",
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textSecondary
                )
                Text(
                    text = "${state.lastPhotoUpdateWeight} • ${state.lastPhotoUpdateDate}",
                    style = TnyxTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun HealthConnectionsCard(onManageClick: () -> Unit) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TnyxTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Watch,
                        contentDescription = null,
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Connect your wearables",
                        style = TnyxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary
                    )
                    Text(
                        text = "Sync steps, workouts, sleep, and recovery from your favorite devices.",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textMuted,
                        lineHeight = 16.sp
                    )
                }
            }
            TnyxPrimaryButton(
                text = "Manage connections",
                onPressed = onManageClick,
                modifier = Modifier.fillMaxWidth()
            )
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
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TnyxTheme.dimens.SpaceM)
            ) {
                Text(
                    text = "BMI :${state.bmi}",
                    style = TnyxTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(TnyxTheme.colors.surfaceVariant)
                                .border(0.5.dp, TnyxTheme.colors.primary.copy(alpha = 0.1f), CircleShape)
                                .clickable { onEditPhoto() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = TnyxTheme.colors.textPrimary,
                                modifier = Modifier.size(36.dp)
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

                    Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))

                    Column {
                        Text(
                            text = state.displayName,
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary
                        )
                        Text(
                            text = state.status,
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ProBadge(state.planLabel)
                    }
                }
            }

            HorizontalDivider(
                thickness = TnyxTheme.dimens.BorderThin,
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TnyxTheme.dimens.SpaceM),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatItem(label = "WEIGHT", value = "${state.weight} kg")
                ProfileStatItem(label = "HEIGHT", value = "${state.height} cm")
                ProfileStatItem(label = "BMR", value = state.bmr.toString())
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = TnyxTheme.colors.textSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = TnyxTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
    }
}

@Composable
private fun ProBadge(label: String) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(TnyxTheme.colors.warning.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Star, null, tint = TnyxTheme.colors.warning, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = label, style = TnyxTheme.typography.labelSmall, color = TnyxTheme.colors.warning, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = TnyxTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TnyxTheme.colors.textPrimary)
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
                Text(text = actionLabel, style = TnyxTheme.typography.labelMedium, color = TnyxTheme.colors.warning)
            }
        }
    }
}

@Composable
private fun LauncherItem(icon: ImageVector, iconTint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    TnyxCard(variant = TnyxCardVariant.Normal, onClick = onClick, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = TnyxTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TnyxTheme.colors.textPrimary)
                Text(text = subtitle, style = TnyxTheme.typography.labelSmall, color = TnyxTheme.colors.textMuted)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = TnyxTheme.colors.textMuted, modifier = Modifier.size(20.dp))
        }
    }
}
