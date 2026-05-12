package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import java.util.Calendar

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StartTimePickerDialogFullPreview() {
    ThenaTheme() {
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.JANUARY, 1)
        val timeInMillis: Long = calendar.timeInMillis

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            StartTimePickerDialog(
                timeInMillis,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StartTimePickerDialogDarkModePreview() {
    ThenaTheme(darkTheme = true) {
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.JANUARY, 1)
        val timeInMillis: Long = calendar.timeInMillis
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            StartTimePickerDialog(
                timeInMillis,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
