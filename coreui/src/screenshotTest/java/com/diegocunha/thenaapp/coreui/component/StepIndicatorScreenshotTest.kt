package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import kotlinx.collections.immutable.persistentListOf

private val labels = persistentListOf("Step 1", "Step 2", "Step 3")
private val previewModifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StepIndicatorFirstStepPreview() {
    ThenaTheme {
        StepIndicator(currentPage = 0, labels = labels, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StepIndicatorFirstStepDarkPreview() {
    ThenaTheme(darkTheme = true) {
        StepIndicator(currentPage = 0, labels = labels, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StepIndicatorMidStepPreview() {
    ThenaTheme {
        StepIndicator(currentPage = 1, labels = labels, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StepIndicatorMidStepDarkPreview() {
    ThenaTheme(darkTheme = true) {
        StepIndicator(currentPage = 1, labels = labels, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StepIndicatorLastStepPreview() {
    ThenaTheme {
        StepIndicator(currentPage = 2, labels = labels, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun StepIndicatorLastStepDarkPreview() {
    ThenaTheme(darkTheme = true) {
        StepIndicator(currentPage = 2, labels = labels, modifier = previewModifier)
    }
}
