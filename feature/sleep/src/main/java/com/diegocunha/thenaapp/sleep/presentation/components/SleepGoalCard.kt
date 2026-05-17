package com.diegocunha.thenaapp.sleep.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.component.CircularProgressWithLabel
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.sleep.presentation.model.SleepDailyStatsUi

@Composable
fun SleepGoalCard(
    stats: SleepDailyStatsUi,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ThenaTheme.extendedColors.sleepFill),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressWithLabel(
                progress = stats.progress,
                label = stats.totalDisplay,
                sublabel = stats.goalDisplay,
                color = ThenaTheme.colors.primary,
                size = 96.dp,
            )
            Spacer(modifier = Modifier.width(ThenaTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sleep_goal_title),
                    style = ThenaTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
                LinearProgressIndicator(
                    progress = { stats.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = ThenaTheme.colors.primary,
                )
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
                val insight = stats.insight
                if (!insight.isNullOrBlank()) {
                    Text(
                        text = insight,
                        style = ThenaTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = stringResource(R.string.sleep_sessions_count, stats.sessionCount),
                    style = ThenaTheme.typography.labelSmall,
                )
            }
        }
    }
}