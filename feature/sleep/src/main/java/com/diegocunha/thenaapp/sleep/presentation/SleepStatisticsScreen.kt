package com.diegocunha.thenaapp.sleep.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.component.LoadingComponent
import com.diegocunha.thenaapp.coreui.component.PagerIndicator
import com.diegocunha.thenaapp.coreui.component.Period
import com.diegocunha.thenaapp.coreui.component.PeriodFilerComponent
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.sleep.presentation.model.SleepDailyStatsUi
import com.diegocunha.thenaapp.sleep.presentation.model.SleepWeeklyStatsUi
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun SleepStatisticsScreen(
    viewModel: SleepStatisticsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val onPeriodSelected = remember(viewModel) {
        { period: SleepStatsPeriod -> viewModel.sendIntent(SleepStatisticsIntent.SelectPeriod(period)) }
    }
    val onPageChanged = remember(viewModel) {
        { index: Int -> viewModel.sendIntent(SleepStatisticsIntent.PageChanged(index)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SleepStatisticsEffect.ShowError ->
                    snackbarHostState.showSnackbar(context.getString(effect.message))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sleep_stats_title)) },
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
            PeriodFilterRow(
                selected = state.selectedPeriod,
                onSelect = onPeriodSelected,
            )
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

            if (state.isLoading) {
                LoadingComponent()
            } else {
                when (state.selectedPeriod) {
                    SleepStatsPeriod.TODAY, SleepStatsPeriod.MONTH -> {
                        val stats = state.dailyStats
                        if (stats == null) {
                            NoDataText()
                        } else {
                            DailyStatsCard(stats = stats)
                        }
                    }
                    SleepStatsPeriod.WEEK -> {
                        val weekly = state.weeklyStats
                        if (weekly == null) {
                            NoDataText()
                        } else {
                            WeeklyAvgCard(weekly = weekly)
                            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))
                            if (weekly.days.isNotEmpty()) {
                                WeeklyBarChart(
                                    days = weekly.days,
                                    currentPageIndex = state.currentPageIndex,
                                    onPageChanged = onPageChanged,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodFilterRow(
    selected: SleepStatsPeriod,
    onSelect: (SleepStatsPeriod) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
    ) {
        SleepStatsPeriod.entries.forEach { period ->
            PeriodFilerComponent(
                modifier = Modifier.weight(1f),
                period = period.toCoreUiPeriod(),
                selected = selected.toCoreUiPeriod(),
                onSelected = { onSelect(SleepStatsPeriod.valueOf(it.name)) },
            )
        }
    }
}

@Composable
private fun NoDataText() {
    Text(
        text = stringResource(R.string.sleep_stats_no_data),
        style = ThenaTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun DailyStatsCard(stats: SleepDailyStatsUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ThenaTheme.extendedColors.sleepFill),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.md),
        ) {
            Text(
                text = stringResource(R.string.sleep_stats_date, stats.date),
                style = ThenaTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatChip(value = stats.totalDisplay, label = stringResource(R.string.sleep_stats_total))
                StatChip(value = stats.sessionCount.toString(), label = stringResource(R.string.sleep_stats_sessions))
                StatChip(value = stats.efficiencyDisplay, label = stringResource(R.string.sleep_stats_efficiency))
            }
            val insight = stats.insight
            if (!insight.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
                Text(text = insight, style = ThenaTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun WeeklyAvgCard(weekly: SleepWeeklyStatsUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ThenaTheme.extendedColors.sleepFill),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            weekly.weeklyAvgDisplay?.let {
                StatChip(value = weekly.weeklyAvgDisplay, label = stringResource(R.string.sleep_stats_weekly_avg))
            }
            StatChip(value = weekly.trend, label = stringResource(R.string.sleep_stats_trend))
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = ThenaTheme.typography.titleSmall)
        Text(text = label, style = ThenaTheme.typography.labelSmall)
    }
}

@Composable
private fun WeeklyBarChart(
    days: PersistentList<SleepDailyStatsUi>,
    currentPageIndex: Int,
    onPageChanged: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = currentPageIndex.coerceIn(0, (days.size - 1).coerceAtLeast(0)),
        pageCount = { days.size },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { onPageChanged(it) }
    }

    Text(
        text = stringResource(R.string.sleep_stats_daily_breakdown),
        style = ThenaTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
    ) { page ->
        DailyBarPage(
            day = days[page],
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = ThenaTheme.spacing.xs),
        )
    }

    if (days.isNotEmpty()) {
        Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
        PagerIndicator(
            pageCount = days.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DailyBarPage(
    day: SleepDailyStatsUi,
    modifier: Modifier = Modifier,
) {
    val barColor = ThenaTheme.extendedColors.sleepFill
    val onSurface = ThenaTheme.colors.onSurface

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ThenaTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = day.date, style = ThenaTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val maxMinutes = maxOf(day.totalSleepMinutes, 1L).toFloat()
                val barWidth = size.width * 0.4f
                val startX = (size.width - barWidth) / 2f
                val barHeight = (day.totalSleepMinutes / maxMinutes) * size.height * 0.8f
                val top = size.height * 0.8f - barHeight
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(startX, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                )
                drawLine(
                    color = onSurface.copy(alpha = 0.2f),
                    start = Offset(0f, size.height * 0.8f),
                    end = Offset(size.width, size.height * 0.8f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
            Text(
                text = day.totalDisplay,
                style = ThenaTheme.typography.bodySmall,
            )
        }
    }
}

private fun SleepStatsPeriod.toCoreUiPeriod(): Period = when (this) {
    SleepStatsPeriod.TODAY -> Period.TODAY
    SleepStatsPeriod.WEEK -> Period.WEEK
    SleepStatsPeriod.MONTH -> Period.MONTH
}
