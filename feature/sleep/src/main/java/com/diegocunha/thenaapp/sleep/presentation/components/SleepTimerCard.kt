package com.diegocunha.thenaapp.sleep.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.coreui.util.formatElapsedSeconds
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.sleep.domain.model.SleepType

@Composable
fun SleepTimerCard(
    isRunning: Boolean,
    elapsedSeconds: Long,
    activeSleepType: SleepType,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSelectType: (SleepType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ThenaTheme.colors
    val gradientBrush = Brush.linearGradient(
        listOf(ThenaTheme.extendedColors.sleepFill, colors.primaryContainer),
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ThenaTheme.extendedColors.sleepFill),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(ThenaTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatElapsedSeconds(elapsedSeconds),
                style = ThenaTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
            if (!isRunning) {
                SleepTypeRow(
                    selected = activeSleepType,
                    onSelect = onSelectType,
                )
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))
            }
            if (isRunning) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.sleep_stop))
                }
            } else {
                Button(onClick = onStart) {
                    Text(stringResource(R.string.sleep_start))
                }
            }
        }
    }
}

@Composable
private fun SleepTypeRow(
    selected: SleepType,
    onSelect: (SleepType) -> Unit,
) {
    val types = listOf(SleepType.NAP, SleepType.NIGHT_SLEEP, SleepType.CATNAP)
    Row(
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
    ) {
        types.forEach { type ->
            val isSelected = type == selected
            if (isSelected) {
                Button(onClick = { onSelect(type) }) {
                    Text(type.displayName())
                }
            } else {
                OutlinedButton(onClick = { onSelect(type) }) {
                    Text(type.displayName())
                }
            }
        }
    }
}

private fun SleepType.displayName() = when (this) {
    SleepType.NAP -> "Nap"
    SleepType.NIGHT_SLEEP -> "Night"
    SleepType.EARLY_MORNING -> "Early"
    SleepType.CATNAP -> "Catnap"
    SleepType.CONTACT_NAP -> "Contact"
    SleepType.CAR_NAP -> "Car"
}