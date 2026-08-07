package com.tnyx.features.workout.presentation.library.createexercise.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

enum class EquipmentItem(
    val displayName: String,
    val assetFileName: String,
) {
    BARBELL("Barbell", "ic_barbell.svg"),
    DUMBBELL("Dumbbell", "ic_dumbbell.svg"),
    BODYWEIGHT("Bodyweight", "ic_bodyweight.svg"),
    CABLE("Cable", "ic_cable.svg"),
    EZ_BARBELL("EZ Barbell", "ic_ez_barbell.svg"),
    KETTLEBELL("Kettlebell", "ic_kettlebell.svg"),
    SMITH_MACHINE("Smith Machine", "ic_smith_machine.svg"),
    LEVERAGE_MACHINE("Leverage Machine", "ic_leverage_machine.webp"),
    BAND("Band", "ic_band.svg"),
    RESISTANCE_BAND("Resistance Band", "ic_resistance_band.svg"),
    WEIGHTED("Weighted", "ic_weighted.svg"),
    TRAP_BAR("Trap Bar", "ic_trap_bar.svg"),
    STABILITY_BALL("Stability Ball", "ic_stability_ball.svg"),
    MEDICINE_BALL("Medicine Ball", "ic_medicine_ball.svg"),
    BOSU_BALL("Bosu Ball", "ic_bosu_ball.svg"),
    JUMP_ROPE("Jump Rope", "ic_jump_rope.svg"),
    BATTLING_ROPE("Battling Rope", "ic_battling_rope.webp"),
    POWER_SLED("Power Sled", "ic_power_sled.svg"),
    SLED_MACHINE("Sled Machine", "ic_sled_machine.svg"),
    SUSPENSION("Suspension", "ic_suspension.svg"),
    WHEEL_ROLLER("Wheel Roller", "ic_wheel_roller.svg"),
    FOAM_ROLL("Foam Roll", "ic_roll.webp"),
    ROLL_BALL("Roll Ball", "ic_rollball.webp"),
}

/**
 * Bottom sheet for selecting an equipment type out of 23 available equipment items.
 * Loads SVG/WebP assets from `assets/ic_equipment/` matching Lyfta parity.
 */
@Composable
fun EquipmentSelectionBottomSheet(
    visible: Boolean,
    selectedEquipment: String,
    onEquipmentSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val equipmentOptions = remember { EquipmentItem.entries }
    val context = LocalContext.current

    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "Select Equipment",
        contentHorizontalPadding = 0.dp,
        contentBottomPadding = 0.dp,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
        ) {
            items(equipmentOptions, key = { it.name }) { equipment ->
                val isSelected = selectedEquipment.equals(equipment.displayName, ignoreCase = true) ||
                        selectedEquipment.equals(equipment.name, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { onEquipmentSelected(equipment.displayName) }
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TnyxTheme.colors.accent.copy(alpha = 0.12f))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = TnyxDimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // ── Equipment Icon (48dp x 48dp, solid white bg) ──
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("file:///android_asset/ic_equipment/${equipment.assetFileName}")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = equipment.displayName,
                                    modifier = Modifier.size(TnyxDimens.SpaceXL)
                                )
                            }

                            Spacer(modifier = Modifier.width(TnyxDimens.SpaceM))

                            Text(
                                text = equipment.displayName,
                                style = TnyxTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.textPrimary,
                            )
                        }

                        // ── Standalone Checkmark indicator (✓ no box/circle) ──
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = TnyxTheme.colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }

                    // ── Inset Divider (1dp, alpha=0.12) ──
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = TnyxDimens.SpaceM)
                            .height(TnyxDimens.BorderThin)
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.12f))
                    )
                }
            }
        }
    }
}
