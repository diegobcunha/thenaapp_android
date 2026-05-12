package com.diegocunha.thenaapp.feature.feeding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun BreastPill(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    elapsed: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val colors = ThenaTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (isActive) colors.secondary else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isActive) colors.secondary else colors.outline,
                shape = RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(vertical = ThenaTheme.spacing.sm, horizontal = ThenaTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides if (isActive) colors.onSecondary else colors.onSurfaceVariant,
                ) { icon() }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = ThenaTheme.typography.titleSmall,
                color = if (isActive) colors.onSecondary else colors.onSurfaceVariant,
            )
        }
        Text(
            text = elapsed,
            style = ThenaTheme.typography.titleMedium,
            color = if (isActive) colors.onSecondary else colors.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BreastPillPreview() {
    ThenaTheme() {
        Column {
            BreastPill(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null
                    )
                },
                label = "label",
                elapsed = "ellapsed",
                isActive = true,
                onClick = {}
            )
        }
    }
}
