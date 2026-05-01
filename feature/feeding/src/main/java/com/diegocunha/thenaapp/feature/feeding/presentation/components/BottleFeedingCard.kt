package com.diegocunha.thenaapp.feature.feeding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingState

@Composable
fun BottleFeedingCard(
    state: FeedingState,
    onMlChange: (String) -> Unit,
    onTypeSelect: (BottleType) -> Unit,
    onSave: () -> Unit,
) {
    val feedFill = ThenaTheme.extendedColors.feedFill
    val gradientBrush = remember(feedFill) {
        Brush.linearGradient(listOf(feedFill, Color(0xFFFFE8F0)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(horizontal = ThenaTheme.spacing.lg, vertical = ThenaTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.feeding_bottle_ml_hint),
                style = ThenaTheme.typography.bodyMedium,
                color = ThenaTheme.colors.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))

            // Quick ml presets
            Row(
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("60", "90", "120", "150", "180").forEach { ml ->
                    val isSelected = state.bottleMl == ml
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) ThenaTheme.colors.secondary
                                else ThenaTheme.colors.secondaryContainer
                            )
                            .clickable { onMlChange(ml) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = ml,
                            style = ThenaTheme.typography.titleSmall,
                            color = if (isSelected) ThenaTheme.colors.onSecondary
                            else ThenaTheme.colors.onSecondaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))

            OutlinedTextField(
                value = state.bottleMl,
                onValueChange = onMlChange,
                label = { Text(stringResource(R.string.feeding_bottle_ml_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("ml") },
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

            // Milk type selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
            ) {
                listOf(
                    BottleType.MOTHERS_MILK to stringResource(R.string.feeding_bottle_mothers_milk),
                    BottleType.POWDERED to stringResource(R.string.feeding_bottle_powdered),
                ).forEach { (type, label) ->
                    val isSelected = state.bottleType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) ThenaTheme.colors.secondaryContainer
                                else ThenaTheme.colors.surfaceContainerHighest
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) ThenaTheme.colors.secondary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable { onTypeSelect(type) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = ThenaTheme.typography.titleSmall,
                            color = if (isSelected) ThenaTheme.colors.onSecondaryContainer
                            else ThenaTheme.colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.xl))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = ThenaTheme.colors.secondary),
            ) {
                Text(stringResource(R.string.feeding_save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottleFeedingCardPreview() {
    ThenaTheme() {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BottleFeedingCard(
                state = FeedingState(),
                onMlChange = {},
                onTypeSelect = {},
                onSave = {}
            )
        }
    }
}
