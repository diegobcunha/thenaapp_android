package com.diegocunha.thenaapp.feature.feeding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun FeedingTypeChip(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ThenaTheme.colors
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) colors.secondaryContainer else colors.surfaceContainerHighest)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) colors.secondary else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$emoji $label",
            style = ThenaTheme.typography.titleSmall,
            color = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        )
    }
}
