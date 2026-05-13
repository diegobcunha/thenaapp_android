package com.diegocunha.thenaapp.coreui.component

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.R
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onConfirm: (Long, Long) -> Unit,
    onDismiss: () -> Unit,
    initialDisplayedMonthMillis: Long? = null,
) {
    val pickerState = rememberDateRangePickerState(
        initialDisplayedMonthMillis = initialDisplayedMonthMillis,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = pickerState.selectedStartDateMillis
                    val end = pickerState.selectedEndDateMillis
                    if (start != null && end != null) onConfirm(start, end)
                },
                enabled = pickerState.selectedStartDateMillis != null &&
                    pickerState.selectedEndDateMillis != null,
            ) {
                Text(stringResource(R.string.coreui_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.coreui_cancel))
            }
        },
    ) {
        DateRangePicker(
            state = pickerState,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DateRangePickerDialogPreview() {
    ThenaTheme {
        DateRangePickerDialog(
            onConfirm = { _, _ -> },
            onDismiss = {},
            initialDisplayedMonthMillis = 1735689600000L, // 2025-01-01 UTC
        )
    }
}
