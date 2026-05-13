package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val activeColor = ThenaTheme.colors.primary
    val inactiveColor = ThenaTheme.colors.onSurface.copy(alpha = 0.3f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = ThenaTheme.spacing.xs)
                    .size(if (index == currentPage) ThenaTheme.spacing.sm else ThenaTheme.spacing.xs),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = if (index == currentPage) activeColor else inactiveColor,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PagerIndicatorPreview() {
    ThenaTheme {
        PagerIndicator(
            pageCount = 5,
            currentPage = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
