package com.diegocunha.thenaapp.sleep.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.diegocunha.thenaapp.coreui.component.StartTimePickerDialog
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.coreui.R as CoreUiR

@Composable
fun LogSleepDialog(
    onConfirm: (startMs: Long, endMs: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var startMs by remember { mutableLongStateOf(System.currentTimeMillis() - 60 * 60 * 1000L) }
    var endMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        StartTimePickerDialog(
            initialStartedAtMs = startMs,
            onConfirm = { startMs = it; showStartPicker = false },
            onDismiss = { showStartPicker = false },
        )
    }

    if (showEndPicker) {
        StartTimePickerDialog(
            initialStartedAtMs = endMs,
            onConfirm = { endMs = it; showEndPicker = false },
            onDismiss = { showEndPicker = false },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sleep_log_dialog_title)) },
        text = {
            Column {
                TimePickerRow(
                    label = stringResource(R.string.sleep_log_start),
                    timeMs = startMs,
                    onClick = { showStartPicker = true },
                )
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
                TimePickerRow(
                    label = stringResource(R.string.sleep_log_end),
                    timeMs = endMs,
                    onClick = { showEndPicker = true },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(startMs, endMs) }) {
                Text(stringResource(CoreUiR.string.coreui_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.coreui_cancel))
            }
        },
    )
}

@Composable
private fun TimePickerRow(
    label: String,
    timeMs: Long,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = ThenaTheme.typography.bodyMedium)
        TextButton(onClick = onClick) {
            Text(
                text = java.text.SimpleDateFormat("HH:mm dd/MM", java.util.Locale.getDefault())
                    .format(java.util.Date(timeMs)),
                style = ThenaTheme.typography.bodyMedium,
            )
        }
    }
}