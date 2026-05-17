package com.diegocunha.thenaapp.sleep.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.sleep.presentation.model.SleepSessionUi

@Composable
fun SleepSessionItem(
    session: SleepSessionUi,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
            ) {
                Text(text = session.typeIcon, style = ThenaTheme.typography.titleMedium)
                Column {
                    Text(
                        text = session.typeName,
                        style = ThenaTheme.typography.bodyMedium,
                    )
                    Text(
                        text = session.timeRange,
                        style = ThenaTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = session.durationDisplay,
                style = ThenaTheme.typography.labelMedium,
            )
        }
    }
}