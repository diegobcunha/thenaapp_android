package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

private val previewModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 12.dp)

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorFirstPagePreview() {
    ThenaTheme {
        PagerIndicator(pageCount = 5, currentPage = 0, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorFirstPageDarkPreview() {
    ThenaTheme(darkTheme = true) {
        PagerIndicator(pageCount = 5, currentPage = 0, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorMidPagePreview() {
    ThenaTheme {
        PagerIndicator(pageCount = 5, currentPage = 2, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorMidPageDarkPreview() {
    ThenaTheme(darkTheme = true) {
        PagerIndicator(pageCount = 5, currentPage = 2, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorLastPagePreview() {
    ThenaTheme {
        PagerIndicator(pageCount = 5, currentPage = 4, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorLastPageDarkPreview() {
    ThenaTheme(darkTheme = true) {
        PagerIndicator(pageCount = 5, currentPage = 4, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorSinglePagePreview() {
    ThenaTheme {
        PagerIndicator(pageCount = 1, currentPage = 0, modifier = previewModifier)
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PagerIndicatorSinglePageDarkPreview() {
    ThenaTheme(darkTheme = true) {
        PagerIndicator(pageCount = 1, currentPage = 0, modifier = previewModifier)
    }
}