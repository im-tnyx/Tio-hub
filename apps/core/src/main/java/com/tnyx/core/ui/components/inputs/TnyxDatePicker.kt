package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tnyx.core.theme.TnyxTheme
import java.time.LocalDate
import java.util.*

@Composable
fun TnyxDatePickerDialog(
    title: String = "Select Date of Birth",
    initialDate: LocalDate = LocalDate.of(1995, 6, 5),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val days = remember { (1..31).map { it.toString().padStart(2, '0') } }
    val months = remember {
        listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val years = remember { (1950..currentYear).map { it.toString() } }

    var selectedDayIndex by remember { mutableIntStateOf(initialDate.dayOfMonth - 1) }
    var selectedMonthIndex by remember { mutableIntStateOf(initialDate.monthValue - 1) }
    var selectedYearIndex by remember { mutableIntStateOf(years.indexOf(initialDate.year.toString()).coerceAtLeast(0)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = TnyxTheme.colors.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TnyxTheme.colors.textPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TnyxTheme.colors.textPrimary)
                        }
                    }

                    Text(
                        text = "We use this data to help personalize TnyX for you",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                        modifier = Modifier.align(Alignment.Start).padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Column Labels
                    Row(modifier = Modifier.fillMaxWidth(0.7f)) {
                        PickerLabel("Day", Modifier.weight(1f))
                        PickerLabel("Month", Modifier.weight(1f))
                        PickerLabel("Year", Modifier.weight(1.2f))
                    }

                    // Wheel Pickers Row
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WheelPicker(
                                items = days,
                                initialIndex = selectedDayIndex,
                                onItemSelected = { selectedDayIndex = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(
                                    topStart = TnyxTheme.dimens.RadiusM,
                                    bottomStart = TnyxTheme.dimens.RadiusM
                                )
                            )

                            WheelPicker(
                                items = months,
                                initialIndex = selectedMonthIndex,
                                onItemSelected = { selectedMonthIndex = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(0.dp)
                            )

                            WheelPicker(
                                items = years,
                                initialIndex = selectedYearIndex,
                                onItemSelected = { selectedYearIndex = it },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(
                                    topEnd = TnyxTheme.dimens.RadiusM,
                                    bottomEnd = TnyxTheme.dimens.RadiusM
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val finalDay = days[selectedDayIndex].toInt()
                            val finalMonth = selectedMonthIndex + 1
                            val finalYear = years[selectedYearIndex].toInt()

                            val finalDate = runCatching {
                                LocalDate.of(finalYear, finalMonth, finalDay)
                            }.getOrElse {
                                LocalDate.of(finalYear, finalMonth, 1).plusMonths(1).minusDays(1)
                            }

                            onConfirm(finalDate)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = TnyxTheme.shapes.Material.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TnyxTheme.colors.primary,
                            contentColor = TnyxTheme.colors.onPrimary
                        )
                    ) {
                        Text("Save", style = TnyxTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PickerLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = TnyxTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = TnyxTheme.colors.textSecondary
    )
}
