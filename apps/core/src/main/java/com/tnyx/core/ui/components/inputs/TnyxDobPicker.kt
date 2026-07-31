package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TnyxDobPickerDialog(
    title: String = "DOB Picker",
    supportingText: String = "We use this data to personalize your experience",
    initialDate: LocalDate = LocalDate.of(2003, 4, 15),
    minimumYear: Int = 1950,
    maximumDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    require(minimumYear <= maximumDate.year) {
        "DOB picker minimum year must not exceed maximum date year"
    }
    val minimumDate = remember(minimumYear) { LocalDate.of(minimumYear, 1, 1) }
    val boundedInitialDate = remember(initialDate, minimumDate, maximumDate) {
        initialDate.coerceIn(minimumDate, maximumDate)
    }
    val days = remember { (1..31).map { it.toString().padStart(2, '0') } }
    val months = remember {
        listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    }
    val years = remember(minimumYear, maximumDate.year) {
        (minimumYear..maximumDate.year).map(Int::toString)
    }

    var selectedDayIndex by remember { mutableIntStateOf(boundedInitialDate.dayOfMonth - 1) }
    var selectedMonthIndex by remember { mutableIntStateOf(boundedInitialDate.monthValue - 1) }
    var selectedYearIndex by remember {
        mutableIntStateOf(years.indexOf(boundedInitialDate.year.toString()).coerceAtLeast(0))
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = TnyxTheme.dimens.RadiusXL,
            topEnd = TnyxTheme.dimens.RadiusXL,
        ),
        containerColor = TnyxTheme.colors.surface,
        contentColor = TnyxTheme.colors.textPrimary,
        tonalElevation = TnyxTheme.elevation.None,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    start = TnyxTheme.dimens.SpaceL,
                    end = TnyxTheme.dimens.SpaceL,
                    top = TnyxTheme.dimens.SpaceL,
                    bottom = TnyxTheme.dimens.SpaceM,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = TnyxTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            }

            Text(
                text = supportingText,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TnyxTheme.dimens.SpaceS),
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            Row(modifier = Modifier.fillMaxWidth(0.7f)) {
                PickerLabel("Day", Modifier.weight(1f))
                PickerLabel("Month", Modifier.weight(1f))
                PickerLabel("Year", Modifier.weight(1.2f))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WheelPicker(
                        items = days,
                        initialIndex = selectedDayIndex,
                        onItemSelected = { selectedDayIndex = it },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(
                            topStart = TnyxTheme.dimens.RadiusM,
                            bottomStart = TnyxTheme.dimens.RadiusM,
                        ),
                    )

                    WheelPicker(
                        items = months,
                        initialIndex = selectedMonthIndex,
                        onItemSelected = { selectedMonthIndex = it },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(TnyxTheme.dimens.SpaceNone),
                    )

                    WheelPicker(
                        items = years,
                        initialIndex = selectedYearIndex,
                        onItemSelected = { selectedYearIndex = it },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(
                            topEnd = TnyxTheme.dimens.RadiusM,
                            bottomEnd = TnyxTheme.dimens.RadiusM,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            TnyxPrimaryButton(
                text = "Save",
                onPressed = {
                    val finalDay = days[selectedDayIndex].toInt()
                    val finalMonth = selectedMonthIndex + 1
                    val finalYear = years[selectedYearIndex].toInt()

                    val finalDate = runCatching {
                        LocalDate.of(finalYear, finalMonth, finalDay)
                    }.getOrElse {
                        LocalDate.of(finalYear, finalMonth, 1).plusMonths(1).minusDays(1)
                    }

                    onConfirm(finalDate.coerceIn(minimumDate, maximumDate))
                },
                modifier = Modifier.fillMaxWidth(),
                expand = true,
            )
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
