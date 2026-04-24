package com.diegocunha.thenaapp.feature.baby.presentation.create.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.baby.R

private data class FeatureItem(val emoji: String, val titleRes: Int, val descRes: Int)

private val featureItems = listOf(
    FeatureItem("🌙", R.string.create_baby_feature_sleep, R.string.create_baby_feature_sleep_desc),
    FeatureItem("🍼", R.string.create_baby_feature_feeding, R.string.create_baby_feature_feeding_desc),
    FeatureItem("💉", R.string.create_baby_feature_vaccines, R.string.create_baby_feature_vaccines_desc),
)

@Composable
fun AllSetStep(
    babyName: String,
    isLoading: Boolean,
    @StringRes generalError: Int?,
    onStartTracking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ThenaTheme.spacing
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(text = "🎉", fontSize = 80.sp)

        Text(
            text = stringResource(R.string.create_baby_success_title, babyName.ifBlank { "Your baby" }),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.create_baby_success_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            featureItems.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = item.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(item.descRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        if (generalError != null) {
            Text(
                text = stringResource(generalError),
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = onStartTracking,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary,
                )
            } else {
                Text(stringResource(R.string.create_baby_start_tracking))
            }
        }
    }
}
