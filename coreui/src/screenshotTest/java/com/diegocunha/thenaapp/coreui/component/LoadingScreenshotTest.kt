package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun LoadingComponentPreview() {
    ThenaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            LoadingComponent()
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun LoadingComponentDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            LoadingComponent()
        }
    }
}