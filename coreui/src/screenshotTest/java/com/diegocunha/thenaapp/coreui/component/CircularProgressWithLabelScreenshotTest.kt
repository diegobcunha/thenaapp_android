package com.diegocunha.thenaapp.coreui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CircularProgressWithLabelLowPreview() {
    ThenaTheme {
        CircularProgressWithLabel(
            progress = 0.25f,
            label = "2h 30m",
            sublabel = "of 10h",
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CircularProgressWithLabelLowDarkPreview() {
    ThenaTheme(darkTheme = true) {
        CircularProgressWithLabel(
            progress = 0.25f,
            label = "2h 30m",
            sublabel = "of 10h",
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CircularProgressWithLabelMidPreview() {
    ThenaTheme {
        CircularProgressWithLabel(
            progress = 0.6f,
            label = "6h",
            sublabel = "of 10h",
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CircularProgressWithLabelMidDarkPreview() {
    ThenaTheme(darkTheme = true) {
        CircularProgressWithLabel(
            progress = 0.6f,
            label = "6h",
            sublabel = "of 10h",
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CircularProgressWithLabelFullPreview() {
    ThenaTheme {
        CircularProgressWithLabel(
            progress = 1f,
            label = "10h",
            sublabel = "of 10h",
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CircularProgressWithLabelFullDarkPreview() {
    ThenaTheme(darkTheme = true) {
        CircularProgressWithLabel(
            progress = 1f,
            label = "10h",
            sublabel = "of 10h",
        )
    }
}
