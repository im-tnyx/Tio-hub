package com.tnyx.features.settings.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

@Composable
fun SettingsHomeScreen(
    uiState: SettingsHomeUiState,
    onAction: (SettingsHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            SettingsTopBar(
                onBack = { onAction(SettingsHomeAction.BackClicked) },
                onSearch = { onAction(SettingsHomeAction.SearchClicked) },
                onNotifications = { onAction(SettingsHomeAction.NotificationsClicked) }
            )
        },
        containerColor = TnyxTheme.colors.background
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(TnyxTheme.dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL)
        ) {
            // User Header
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Glass,
                    onClick = { onAction(SettingsHomeAction.ProfileHeaderClicked) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TnyxTheme.colors.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.displayName,
                                style = TnyxTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TnyxTheme.colors.textPrimary
                            )
                            Text(
                                text = uiState.email,
                                style = TnyxTheme.typography.bodySmall,
                                color = TnyxTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingsProBadge(uiState.planLabel)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textMuted
                        )
                    }
                }
            }

            // Account Section
            item {
                SettingsSection(title = "Account") {
                    SettingsItem(
                        icon = Icons.Rounded.Person,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Personal Information",
                        subtitle = "Update your personal details",
                        onClick = { onAction(SettingsHomeAction.PersonalInfoClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Star,
                        iconColor = TnyxTheme.colors.success,
                        title = "Subscription",
                        subtitle = "Manage your plan and billing",
                        onClick = { onAction(SettingsHomeAction.SubscriptionClicked) }
                    )
                }
            }

            // Preferences Section
            item {
                SettingsSection(title = "Preferences") {
                    SettingsItem(
                        icon = Icons.Rounded.Settings,
                        iconColor = TnyxTheme.colors.primary,
                        title = "App Preferences",
                        subtitle = "Theme, language, units & more",
                        onClick = { onAction(SettingsHomeAction.AppPreferencesClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Notifications,
                        iconColor = TnyxTheme.colors.warning,
                        title = "Notifications",
                        subtitle = "Manage reminders and alerts",
                        onClick = { onAction(SettingsHomeAction.ManageNotificationsClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Translate,
                        iconColor = TnyxTheme.colors.info,
                        title = "Language",
                        subtitle = "Choose your app language",
                        value = "English",
                        onClick = { onAction(SettingsHomeAction.LanguageClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Square,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Units",
                        subtitle = "Metric (kg, cm)",
                        value = "Metric",
                        onClick = { onAction(SettingsHomeAction.UnitsClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Share,
                        iconColor = TnyxTheme.colors.warning,
                        title = "Export Data",
                        subtitle = "Download your data",
                        onClick = { onAction(SettingsHomeAction.ExportDataClicked) }
                    )
                }
            }

            // Support & About Section
            item {
                SettingsSection(title = "Support & About") {
                    SettingsItem(
                        icon = Icons.Rounded.Info,
                        iconColor = TnyxTheme.colors.info,
                        title = "About",
                        subtitle = "Version, terms and privacy",
                        onClick = { onAction(SettingsHomeAction.AboutClicked) }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        iconColor = TnyxTheme.colors.success,
                        title = "Help & FAQ",
                        subtitle = "Get help and find answers",
                        onClick = { onAction(SettingsHomeAction.HelpFaqClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Email,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Contact Us",
                        subtitle = "We're here to help",
                        onClick = { onAction(SettingsHomeAction.ContactUsClicked) }
                    )
                }
            }

            // Logout Section
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    onClick = { onAction(SettingsHomeAction.LogoutClicked) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TnyxTheme.colors.error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = TnyxTheme.colors.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Logout",
                                style = TnyxTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TnyxTheme.colors.error
                            )
                            Text(
                                text = "Sign out from your account",
                                style = TnyxTheme.typography.labelSmall,
                                color = TnyxTheme.colors.textMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = TnyxTheme.dimens.SpaceM, vertical = TnyxTheme.dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = TnyxTheme.colors.textPrimary)
        }
        Text(
            text = "Settings",
            style = TnyxTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, null, tint = TnyxTheme.colors.textPrimary)
        }
        IconButton(onClick = onNotifications) {
            Icon(Icons.Default.Notifications, null, tint = TnyxTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
        Text(
            text = title,
            style = TnyxTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textMuted
        )
        TnyxCard(variant = TnyxCardVariant.Normal, padding = 0.dp) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TnyxTheme.dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TnyxTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = TnyxTheme.typography.labelSmall,
                    color = TnyxTheme.colors.textMuted
                )
            }
            if (value != null) {
                Text(
                    text = value,
                    style = TnyxTheme.typography.labelMedium,
                    color = TnyxTheme.colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsProBadge(label: String) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(TnyxTheme.colors.warning.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = TnyxTheme.colors.warning,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = TnyxTheme.colors.warning,
            fontWeight = FontWeight.Bold
        )
    }
}
