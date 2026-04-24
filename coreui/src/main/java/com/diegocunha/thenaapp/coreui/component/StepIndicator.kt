package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList


@Composable
fun StepIndicator(
    currentPage: Int,
    labels: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val space = ThenaTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space.sm),
    ) {
        labels.forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space.xs),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(space.xs)
                        .clip(RoundedCornerShape(space.xxs))
                        .background(
                            if (index <= currentPage) colors.primary else colors.outlineVariant
                        )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == currentPage) colors.primary else colors.onSurfaceVariant,
                    fontWeight = if (index == currentPage) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Preview
@Composable
private fun StepIndicatorPreview() {
    ThenaTheme {
        StepIndicator(
            currentPage = 0,
            labels = listOf("Label", "Label 2").toImmutableList(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}
