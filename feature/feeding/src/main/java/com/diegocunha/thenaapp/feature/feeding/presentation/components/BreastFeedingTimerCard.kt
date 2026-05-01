package com.diegocunha.thenaapp.feature.feeding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingState

@Composable
fun BreastfeedingTimerCard(
    state: FeedingState,
    onTapBreast: (Breast) -> Unit,
    onFinish: () -> Unit,
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
                text = formatElapsed(state.totalElapsedSeconds),
                fontFamily = ThenaTheme.typography.displayMedium.fontFamily,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = ThenaTheme.colors.secondary,
                letterSpacing = 2.sp,
            )
            Text(
                text = if (state.activeBreast != null)
                    stringResource(R.string.feeding_active)
                else if (state.sessionId != null)
                    stringResource(R.string.feeding_paused)
                else
                    stringResource(R.string.feeding_tap_to_begin),
                style = ThenaTheme.typography.bodySmall,
                color = ThenaTheme.colors.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
            ) {
                BreastPill(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.feeding_breast_left),
                    elapsed = formatElapsed(state.leftElapsedSeconds),
                    isActive = state.activeBreast == Breast.LEFT,
                    onClick = { onTapBreast(Breast.LEFT) },
                )
                BreastPill(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.feeding_breast_right),
                    elapsed = formatElapsed(state.rightElapsedSeconds),
                    isActive = state.activeBreast == Breast.RIGHT,
                    onClick = { onTapBreast(Breast.RIGHT) },
                )
            }

            state.sessionId?.let {
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.xl))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThenaTheme.colors.secondary,
                    ),
                ) {
                    Text(stringResource(R.string.feeding_finish))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BreastfeedingTimerCardPreview() {
    ThenaTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BreastfeedingTimerCard(
                state = FeedingState(),
                onTapBreast = {},
                onFinish = {}
            )
        }
    }
}

private fun formatElapsed(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun BreastfeedingActivePreview() {
    ThenaTheme {
        BreastfeedingTimerCard(
            state = FeedingState(
                sessionId = "1",
                feedingType = FeedingType.BREAST,
                activeBreast = Breast.LEFT,
                leftElapsedSeconds = 483L,
                rightElapsedSeconds = 120L,
                totalElapsedSeconds = 603L,
            ),
            onTapBreast = {},
            onFinish = {},
        )
    }
}
