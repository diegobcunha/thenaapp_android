package com.diegocunha.thenaapp.coreui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

// Fixed to January 2025 so goldens don't drift as the calendar advances.
private const val JANUARY_2025_MS = 1735689600000L

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@Preview(showBackground = true)
@Composable
fun DateRangePickerDialogLightPreview() {
    ThenaTheme {
        DateRangePickerDialog(
            onConfirm = { _, _ -> },
            onDismiss = {},
            initialDisplayedMonthMillis = JANUARY_2025_MS,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@Preview(showBackground = true)
@Composable
fun DateRangePickerDialogDarkPreview() {
    ThenaTheme(darkTheme = true) {
        DateRangePickerDialog(
            onConfirm = { _, _ -> },
            onDismiss = {},
            initialDisplayedMonthMillis = JANUARY_2025_MS,
        )
    }
}