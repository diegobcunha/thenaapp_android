package com.diegocunha.thenaapp.sleep.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.diegocunha.thenaapp.coreui.icon.ThenaIcon
import com.diegocunha.thenaapp.coreui.icon.ThenaIcons
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
                ThenaIcon(icon = session.typeIcon.toThenaIcon(), modifier = Modifier.size(ThenaTheme.spacing.lg))
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

private fun String.toThenaIcon(): ThenaIcon = when (this) {
    "NAP" -> ThenaIcons.Nap
    "NIGHT_SLEEP" -> ThenaIcons.Sleep
    "EARLY_MORNING" -> ThenaIcons.EarlyMorning
    "CATNAP" -> ThenaIcons.Catnap
    "CONTACT_NAP" -> ThenaIcons.Breastfeeding
    "CAR_NAP" -> ThenaIcons.CarNap
    else -> ThenaIcons.Sleep
}