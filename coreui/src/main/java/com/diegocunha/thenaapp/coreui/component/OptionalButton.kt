package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.icon.ThenaIcon
import com.diegocunha.thenaapp.coreui.icon.ThenaIcons
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun OptionalButton(
    label: String,
    icon: ThenaIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    val space = ThenaTheme.spacing
    val contentColor = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
    Box(
        modifier = modifier
            .height(space.xl)
            .clip(shape)
            .background(if (selected) colors.secondaryContainer else colors.surfaceVariant)
            .border(
                width = space.xxs,
                color = if (selected) colors.secondary else Color.Transparent,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThenaIcon(icon = icon, modifier = Modifier.size(ThenaTheme.spacing.md), tint = contentColor)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewOptionalButton() {
    ThenaTheme {
        Row {
            OptionalButton(
                label = "label",
                icon = ThenaIcons.Blossom,
                selected = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
