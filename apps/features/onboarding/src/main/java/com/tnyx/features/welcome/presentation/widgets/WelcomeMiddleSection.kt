package com.tnyx.features.welcome.presentation.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme

@Composable
fun WelcomeMiddleSection(
    title: String,
    features: List<String>
) {
    Column {
        Text(
            text = title,
            style = TnyxTheme.typography.headlineLarge,
            color = TnyxTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

        Column {
            features.forEachIndexed { index, feature ->
                val icon = when (index) {
                    0 -> Icons.Outlined.PhotoCamera
                    1 -> Icons.Outlined.FitnessCenter
                    2 -> Icons.Outlined.Psychology
                    else -> Icons.Outlined.CheckCircle
                }
                FeatureItem(
                    text = feature,
                    icon = icon
                )
            }
        }
    }
}
