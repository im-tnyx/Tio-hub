package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme

@Composable
fun NutritionVitaminSection(
    onOverviewRequested: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TnyxTheme.colors.surfaceRaised)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vitamins
            VitaminColumn(
                label = "Vitamins",
                icon = Icons.Outlined.Eco,
                progress = 0.6f,
                onTap = { onOverviewRequested("vitamins") },
                modifier = Modifier.weight(1f)
            )
            
            // Minerals
            VitaminColumn(
                label = "Minerals",
                icon = Icons.Outlined.Cookie,
                progress = 0.3f,
                onTap = { onOverviewRequested("minerals") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VitaminColumn(
    label: String,
    icon: ImageVector,
    progress: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onTap() }
    ) {
        SectionHeader(label = label, icon = icon, progress = progress)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VitaminTag(label = "Vit D", status = "very low", percentage = "(6%)", modifier = Modifier.weight(1f))
            VitaminTag(label = "Calcium", status = "very low", percentage = "(0%)", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionHeader(
    label: String,
    icon: ImageVector,
    progress: Float
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TnyxTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = TnyxTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = TnyxTheme.colors.textSecondary,
            trackColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.12f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            style = TnyxTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
    }
}

@Composable
private fun VitaminTag(
    label: String,
    status: String,
    percentage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TnyxTheme.colors.error.copy(alpha = 0.1f))
            .border(0.5.dp, TnyxTheme.colors.error.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.labelMedium,
            color = TnyxTheme.colors.error,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = status,
            style = TnyxTheme.typography.labelSmall,
            color = TnyxTheme.colors.error.copy(alpha = 0.6f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = percentage,
            style = TnyxTheme.typography.labelSmall,
            color = TnyxTheme.colors.error.copy(alpha = 0.6f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
    }
}
