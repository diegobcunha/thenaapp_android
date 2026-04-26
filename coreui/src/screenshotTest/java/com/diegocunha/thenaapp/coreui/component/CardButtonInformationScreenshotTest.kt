package com.diegocunha.thenaapp.coreui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CardButtonInformationFullPreview() {
    ThenaTheme {
        CardButtonInformation(
            headerInformation = { Text("Header") },
            titleInformation = { Text("Hello world") },
            subtitleInformation = { Text("Subtitle information") }
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CardButtonInformationFullDarkPreview() {
    ThenaTheme(darkTheme = true) {
        CardButtonInformation(
            headerInformation = { Text("Header") },
            titleInformation = { Text("Hello world") },
            subtitleInformation = { Text("Subtitle information") }
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CardButtonInformationNoSubtitlePreview() {
    ThenaTheme {
        CardButtonInformation(
            headerInformation = { Text("Header") },
            titleInformation = { Text("Hello world") }
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CardButtonInformationNoSubtitleDarkPreview() {
    ThenaTheme(darkTheme = true) {
        CardButtonInformation(
            headerInformation = { Text("Header") },
            titleInformation = { Text("Hello world") }
        )
    }
}
