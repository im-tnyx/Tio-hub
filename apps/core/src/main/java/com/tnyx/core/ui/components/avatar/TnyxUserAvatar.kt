package com.tnyx.core.ui.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.shared.profile.domain.model.MembershipTier

private val PremiumHexagonShape = GenericShape { size, _ ->
    moveTo(size.width * 0.25f, 0f)
    lineTo(size.width * 0.75f, 0f)
    lineTo(size.width, size.height * 0.5f)
    lineTo(size.width * 0.75f, size.height)
    lineTo(size.width * 0.25f, size.height)
    lineTo(0f, size.height * 0.5f)
    close()
}

enum class TnyxAvatarSize(
    val containerSize: Dp,
    val imageSize: Dp,
    val badgeSize: Dp,
    val fallbackIconSize: Dp,
) {
    Small(
        containerSize = 36.dp,
        imageSize = 30.dp,
        badgeSize = 14.dp,
        fallbackIconSize = 16.dp,
    ),
    Medium(
        containerSize = 44.dp,
        imageSize = 36.dp,
        badgeSize = 16.dp,
        fallbackIconSize = 20.dp,
    ),
    Large(
        containerSize = 76.dp,
        imageSize = 64.dp,
        badgeSize = 22.dp,
        fallbackIconSize = 32.dp,
    ),
}

@Composable
fun TnyxUserAvatar(
    imageUrl: String?,
    displayName: String,
    membershipTier: MembershipTier,
    modifier: Modifier = Modifier,
    size: TnyxAvatarSize = TnyxAvatarSize.Medium,
    onClick: (() -> Unit)? = null,
    showEditBadge: Boolean = false,
    onEditClick: (() -> Unit)? = null,
) {
    val frameShape = when (membershipTier) {
        MembershipTier.Premium -> PremiumHexagonShape
        MembershipTier.Free,
        MembershipTier.Plus,
        -> CircleShape
    }
    val frameBrush = when (membershipTier) {
        MembershipTier.Free -> SolidColor(TnyxTheme.colors.surfaceVariant)
        MembershipTier.Plus -> Brush.linearGradient(
            colors = listOf(
                TnyxTheme.colors.success,
                TnyxTheme.colors.primary,
            ),
        )
        MembershipTier.Premium -> Brush.linearGradient(
            colors = listOf(
                TnyxTheme.colors.warning,
                TnyxTheme.colors.primary,
                TnyxTheme.colors.info,
            ),
        )
    }
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    val accessibleName = displayName.ifBlank { "User" }

    Box(
        modifier = modifier
            .size(size.containerSize)
            .semantics {
                contentDescription = "$accessibleName profile photo, ${membershipTier.name.lowercase()} plan"
            }
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size.containerSize)
                .clip(frameShape)
                .background(frameBrush)
                .padding(if (membershipTier == MembershipTier.Free) 1.dp else 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size.imageSize)
                    .clip(CircleShape)
                    .background(TnyxTheme.colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                val resolvedUrl = imageUrl?.takeIf(String::isNotBlank)
                if (resolvedUrl == null) {
                    AvatarFallback(
                        displayName = displayName,
                        iconSize = size.fallbackIconSize,
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = resolvedUrl,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            AvatarFallback(
                                displayName = displayName,
                                iconSize = size.fallbackIconSize,
                            )
                        },
                        error = {
                            AvatarFallback(
                                displayName = displayName,
                                iconSize = size.fallbackIconSize,
                            )
                        },
                        success = { SubcomposeAsyncImageContent() },
                    )
                }
            }
        }

        when (membershipTier) {
            MembershipTier.Free -> Unit
            MembershipTier.Plus -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(size.badgeSize)
                        .clip(CircleShape)
                        .background(TnyxTheme.colors.success),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+",
                        style = TnyxTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = TnyxTheme.colors.onPrimary,
                    )
                }
            }
            MembershipTier.Premium -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(size.badgeSize)
                        .clip(CircleShape)
                        .background(TnyxTheme.colors.warning),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = TnyxTheme.colors.onPrimary,
                        modifier = Modifier.size(size.badgeSize * 0.65f),
                    )
                }
            }
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size.badgeSize)
                    .clip(CircleShape)
                    .background(TnyxTheme.colors.textPrimary)
                    .then(
                        if (onEditClick != null) {
                            Modifier.clickable(onClick = onEditClick)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Change profile photo",
                    tint = TnyxTheme.colors.background,
                    modifier = Modifier.size(size.badgeSize * 0.55f),
                )
            }
        }
    }
}

@Composable
private fun AvatarFallback(
    displayName: String,
    iconSize: Dp,
) {
    val initials = avatarInitials(displayName)
    if (initials.isNotEmpty()) {
        Text(
            text = initials,
            style = TnyxTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
        )
    } else {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = TnyxTheme.colors.textPrimary,
            modifier = Modifier.size(iconSize),
        )
    }
}

internal fun avatarInitials(displayName: String): String {
    return displayName
        .trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
        .take(2)
        .mapNotNull { part -> part.firstOrNull()?.uppercaseChar() }
        .joinToString(separator = "")
}
