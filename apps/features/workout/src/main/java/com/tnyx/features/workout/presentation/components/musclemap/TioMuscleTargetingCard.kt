package com.tnyx.features.workout.presentation.components.musclemap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

/**
 * Central reusable card component that displays real-time Front & Back anatomical human body maps
 * highlighting primary (Red) and secondary (Blue) muscle groups selected by the user.
 *
 * Owned by `presentation/components/musclemap/` for cross-feature reusability.
 */
@Composable
fun TioMuscleTargetingCard(
    primaryMuscleGroup: String,
    otherMuscles: String,
    modifier: Modifier = Modifier,
    variant: ExerciseMediaVariant = ExerciseMediaVariant.MALE,
) {
    val selectedPrimaryList = remember(primaryMuscleGroup) {
        primaryMuscleGroup
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals("Select", ignoreCase = true) }
    }
    val selectedSecondaryList = remember(otherMuscles) {
        otherMuscles
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.contains("optional", ignoreCase = true) && !it.equals("Select", ignoreCase = true) }
    }

    if (selectedPrimaryList.isEmpty() && selectedSecondaryList.isEmpty()) return

    TnyxCard(
        modifier = modifier.fillMaxWidth(),
        variant = TnyxCardVariant.Surface,
        shape = RoundedCornerShape(TnyxDimens.RadiusM),
        padding = TnyxDimens.SpaceM
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Muscle Targeting",
                    style = TnyxTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TnyxTheme.colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TnyxTheme.colors.accent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Primary",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DefaultSecondaryMuscleColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Secondary",
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxDimens.SpaceS))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Front View Map
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    TioMuscleMap(
                        muscleGroups = selectedPrimaryList,
                        secondaryMuscles = selectedSecondaryList,
                        variant = variant,
                        view = MuscleMapView.FRONT,
                        contentDescription = "Front Muscle Map",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Back View Map
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    TioMuscleMap(
                        muscleGroups = selectedPrimaryList,
                        secondaryMuscles = selectedSecondaryList,
                        variant = variant,
                        view = MuscleMapView.BACK,
                        contentDescription = "Back Muscle Map",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
