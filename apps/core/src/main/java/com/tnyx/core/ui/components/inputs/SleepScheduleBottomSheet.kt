package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

/**
 * Shared editor for the two values that form a sleep schedule.
 * The caller owns persistence and supplies the initial values, so the sheet
 * can be reused wherever users edit their bedtime and wake-up time together.
 */
@Composable
fun SleepScheduleBottomSheet(
    visible: Boolean,
    sleepTime: String,
    wakeTime: String,
    onDismissRequest: () -> Unit,
    onSave: (sleepTime: String, wakeTime: String) -> Unit,
) {
    if (!visible) return

    var updatedSleepTime by remember(sleepTime) { mutableStateOf(sleepTime) }
    var updatedWakeTime by remember(wakeTime) { mutableStateOf(wakeTime) }

    TnyxModalBottomSheet(
        visible = true,
        title = "Sleep schedule",
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = "Set your bedtime and wake-up time.",
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textMuted,
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
        OutlinedTextField(
            value = updatedSleepTime,
            onValueChange = { updatedSleepTime = it },
            label = { Text("Bed time") },
            placeholder = { Text("10:00 PM") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
        OutlinedTextField(
            value = updatedWakeTime,
            onValueChange = { updatedWakeTime = it },
            label = { Text("Wake up time") },
            placeholder = { Text("06:00 AM") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
        TnyxPrimaryButton(
            text = "Save schedule",
            onPressed = { onSave(updatedSleepTime, updatedWakeTime) },
            expand = true,
        )
    }
}
