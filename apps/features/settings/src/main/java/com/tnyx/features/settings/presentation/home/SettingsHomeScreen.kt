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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
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
                onBack = { onAction(SettingsHomeAction.BackClicked) }
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
            // Pro upgrade card
            item {
                SettingsProUpgradeCard(
                    onUnlock = { onAction(SettingsHomeAction.SubscriptionClicked) }
                )
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
                        iconColor = TnyxTheme.colors.primary,
                        title = "Manage Subscription",
                        subtitle = "Manage your plan and billing",
                        onClick = { onAction(SettingsHomeAction.SubscriptionClicked) }
                    )
                }
            }

            // Quick Actions Section
            item {
                SettingsSection(title = "Quick Actions") {
                    SettingsItem(
                        icon = Icons.Rounded.Restaurant,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Nutrition Targets",
                        subtitle = "Calories, macros, water & more",
                        onClick = { onAction(SettingsHomeAction.NutritionTargetsClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Accessibility,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Workout Settings",
                        subtitle = "Rest timer, warm-up, plates & more",
                        onClick = { onAction(SettingsHomeAction.WorkoutSettingsClicked) }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.BarChart,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Graph Settings",
                        subtitle = "Customize your progress graphs",
                        onClick = { onAction(SettingsHomeAction.GraphSettingsClicked) }
                    )
                }
            }

            // App Settings Section
            item {
                SettingsSection(title = "App Settings") {
                    SettingsItem(
                        icon = Icons.Rounded.Tune,
                        iconColor = TnyxTheme.colors.primary,
                        title = "App Preferences",
                        subtitle = "Theme, language, units & more",
                        onClick = { onAction(SettingsHomeAction.AppPreferencesClicked) }
                    )
                }
            }

            // Support & About Section
            item {
                SettingsSection(title = "Support & About") {
                    SettingsItem(
                        icon = Icons.Rounded.Info,
                        iconColor = TnyxTheme.colors.primary,
                        title = "About",
                        subtitle = "Version, terms and privacy",
                        onClick = { onAction(SettingsHomeAction.AboutClicked) }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        iconColor = TnyxTheme.colors.primary,
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

            // More Section
            item {
                SettingsSection(title = "More") {
                    SettingsItem(
                        icon = Icons.Rounded.CardGiftcard,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Rewards",
                        subtitle = "Refer friends, earn rewards",
                        onClick = { onAction(SettingsHomeAction.RewardsClicked) }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        iconColor = TnyxTheme.colors.primary,
                        title = "Resources",
                        subtitle = "Guides, articles & tools",
                        onClick = { onAction(SettingsHomeAction.ResourcesClicked) }
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
private fun SettingsProUpgradeCard(
    onUnlock: () -> Unit
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = TnyxTheme.dimens.SpaceM
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Stars,
                contentDescription = null,
                tint = TnyxTheme.colors.warning,
                modifier = Modifier.size(TnyxTheme.dimens.IconM)
            )
            Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
            Text(
                text = "TNYX PRO",
                style = TnyxTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onUnlock,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TnyxTheme.colors.warning,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(
                    horizontal = TnyxTheme.dimens.SpaceM,
                    vertical = TnyxTheme.dimens.SpaceS
                )
            ) {
                Text(
                    text = "Unlock",
                    style = TnyxTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit
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
    showDivider: Boolean = true, // <-- यहाँ नया पैरामीटर जोड़ा गया है (डिफ़ॉल्ट रूप से true)
    onClick: () -> Unit
) {
    Column {
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = TnyxTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXS))
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

        // <-- यहाँ डिवाइडर की कंडीशन लगाई गई है
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp), // टेक्स्ट के नीचे से शुरू करने के लिए 56.dp की पैडिंग
                thickness = 0.5.dp,
                color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
            )
        }
    }
}