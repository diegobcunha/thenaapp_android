package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.presentation.components.BottleFeedingCard
import com.diegocunha.thenaapp.feature.feeding.presentation.components.BreastfeedingTimerCard
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                FeedingEffect.NavigateBack -> onNavigateBack()
                is FeedingEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feeding_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ThenaTheme.spacing.lg, vertical = ThenaTheme.spacing.md),
        ) {
            FeedingTypePicker(
                selected = state.feedingType,
                onSelect = { type ->
                    when (type) {
                        FeedingType.BREAST -> viewModel.sendIntent(FeedingIntent.SelectBreastfeeding)
                        FeedingType.BOTTLE -> viewModel.sendIntent(FeedingIntent.SelectBottle)
                    }
                },
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

            when (state.feedingType) {
                null, FeedingType.BREAST -> BreastfeedingTimerCard(
                    state = state,
                    onTapBreast = { viewModel.sendIntent(FeedingIntent.TapBreast(it)) },
                    onFinish = { viewModel.sendIntent(FeedingIntent.StopSession) },
                )

                FeedingType.BOTTLE -> BottleFeedingCard(
                    state = state,
                    onMlChange = { viewModel.sendIntent(FeedingIntent.UpdateBottleMl(it)) },
                    onTypeSelect = { viewModel.sendIntent(FeedingIntent.SelectBottleType(it)) },
                    onSave = { viewModel.sendIntent(FeedingIntent.SaveBottleFeeding) },
                )
            }
        }
    }
}

@Composable
private fun FeedingTypePicker(
    selected: FeedingType?,
    onSelect: (FeedingType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
    ) {
        FeedingTypeChip(
            modifier = Modifier.weight(1f),
            emoji = "🤱",
            label = stringResource(R.string.feeding_type_breastfeed),
            isSelected = selected == null || selected == FeedingType.BREAST,
            onClick = { onSelect(FeedingType.BREAST) },
        )
        FeedingTypeChip(
            modifier = Modifier.weight(1f),
            emoji = "🍼",
            label = stringResource(R.string.feeding_type_bottle),
            isSelected = selected == FeedingType.BOTTLE,
            onClick = { onSelect(FeedingType.BOTTLE) },
        )
    }
}

@Composable
private fun FeedingTypeChip(
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

@Preview(showBackground = true)
@Composable
private fun BottleFeedingPreview() {
    ThenaTheme {
        BottleFeedingCard(
            state = FeedingState(
                feedingType = FeedingType.BOTTLE,
                bottleType = BottleType.MOTHERS_MILK,
                bottleMl = "120",
            ),
            onMlChange = {},
            onTypeSelect = {},
            onSave = {},
        )
    }
}
