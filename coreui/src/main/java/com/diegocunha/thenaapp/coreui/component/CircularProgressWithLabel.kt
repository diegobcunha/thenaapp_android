package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun CircularProgressWithLabel(
    progress: Float,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = ThenaTheme.colors.primary,
    strokeWidth: Dp = 8.dp,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = ThenaTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = sublabel,
                style = ThenaTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun CircularProgressWithLabelPreview() {
    ThenaTheme {
        CircularProgressWithLabel(
            progress = 0.6f,
            label = "6h",
            sublabel = "of 10h",
        )
    }
}
