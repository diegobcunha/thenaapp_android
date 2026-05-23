package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.core.util.toDisplayDate
import com.diegocunha.thenaapp.coreui.component.DateRangePickerDialog
import com.diegocunha.thenaapp.coreui.component.LoadingComponent
import com.diegocunha.thenaapp.coreui.component.PagerIndicator
import com.diegocunha.thenaapp.coreui.component.Period
import com.diegocunha.thenaapp.coreui.component.PeriodFilerComponent
import com.diegocunha.thenaapp.coreui.icon.ThenaIcon
import com.diegocunha.thenaapp.coreui.icon.ThenaIcons
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.feeding.R
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun FeedingStatisticsScreen(
    viewModel: FeedingStatisticsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val onpPeriodSelected = remember(viewModel) {
        { period: FeedingStatsPeriod ->
            viewModel.sendIntent(
                FeedingStatisticsIntent.SelectPeriod(period)
            )
        }
    }
    val onPeriodChange = remember(viewModel) {
        { page: Int ->
            viewModel.sendIntent(
                FeedingStatisticsIntent.PageChanged(
                    page
                )
            )
        }
    }
    val onRageSelected = remember(viewModel) {
        { start: Long, end: Long ->
            viewModel.sendIntent(
                FeedingStatisticsIntent.SelectCustomDateRange(
                    start,
                    end
                )
            )
        }
    }
    val onDismissPicker = remember(viewModel) {
        {
            viewModel.sendIntent(FeedingStatisticsIntent.DismissDateRangePicker)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is FeedingStatisticsEffect.ShowError ->
                    snackbarHostState.showSnackbar(context.getString(effect.message))
            }
        }
    }

    FeedingStatisticsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onpPeriodSelected = onpPeriodSelected,
        onPeriodChange = onPeriodChange,
        onRageSelected = onRageSelected,
        onDismissPicker = onDismissPicker,
    )

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FeedingStatisticsContent(
    state: FeedingStatisticsState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onpPeriodSelected: (FeedingStatsPeriod) -> Unit,
    onPeriodChange: (Int) -> Unit,
    onRageSelected: (Long, Long) -> Unit,
    onDismissPicker: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feeding_stats_title)) },
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
                selected = Period.valueOf(state.selectedPeriod.name),
                onSelect = { onpPeriodSelected(FeedingStatsPeriod.valueOf(it.name)) },
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

            if (state.isLoading) {
                LoadingComponent()
            } else {
                val stats = state.statistics
                if (stats == null) {
                    Text(
                        text = stringResource(R.string.feeding_stats_no_data),
                        style = ThenaTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    AggregateStatsSection(stats = stats)
                    Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

                    if (stats.volumeByMilkType.isNotEmpty()) {
                        MilkTypeCard(volumeByMilkType = stats.volumeByMilkType)
                        Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))
                    }

                    if (stats.dailyBreakdown.isNotEmpty()) {
                        DailyBreakdownSection(
                            dailyBreakdown = stats.dailyBreakdown,
                            currentPageIndex = state.currentPageIndex,
                            onPageChanged = onPeriodChange,
                        )
                    }
                }
            }
        }
    }

    if (state.showDateRangePicker) {
        DateRangePickerDialog(
            onConfirm = onRageSelected,
            onDismiss = onDismissPicker,
        )
    }
}

@Composable
private fun PeriodFilterRow(
    selected: Period,
    onSelect: (Period) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
    ) {
        PeriodFilerComponent(
            modifier = Modifier.weight(1f),
            period = Period.TODAY,
            selected = selected,
            onSelected = onSelect
        )

        PeriodFilerComponent(
            modifier = Modifier.weight(1f),
            period = Period.WEEK,
            selected = selected,
            onSelected = onSelect
        )

        PeriodFilerComponent(
            modifier = Modifier.weight(1f),
            period = Period.MONTH,
            selected = selected,
            onSelected = onSelect
        )

        PeriodFilerComponent(
            modifier = Modifier.weight(1f),
            period = Period.CUSTOM,
            selected = selected,
            onSelected = onSelect
        )
    }
}

@Composable
private fun AggregateStatsSection(stats: FeedingStatisticsInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = ThenaIcons.Bottle,
            value = stats.totalSessions.toString(),
            label = stringResource(R.string.feeding_stats_total_sessions),
            color = ThenaTheme.extendedColors.feedFill,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = ThenaIcons.Breastfeeding,
            value = stats.breastfeedingSessions.toString(),
            label = stringResource(R.string.feeding_stats_breastfeeding),
            color = ThenaTheme.colors.primaryContainer,
        )
    }
    Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = ThenaIcons.Timer,
            value = "${stats.averageBreastfeedingDurationSeconds / 60}",
            label = stringResource(R.string.feeding_stats_avg_duration_min, "min"),
            color = ThenaTheme.colors.secondaryContainer,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = ThenaIcons.Bottle,
            value = "${stats.totalBottleVolumeMl}ml",
            label = stringResource(R.string.feeding_stats_total_volume_ml, ""),
            color = ThenaTheme.extendedColors.vaccineFill,
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ThenaIcon,
    value: String,
    label: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ThenaIcon(icon = icon, modifier = Modifier.size(ThenaTheme.spacing.lg))
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.xxs))
            Text(text = value, style = ThenaTheme.typography.titleLarge)
            Text(
                text = label,
                style = ThenaTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MilkTypeCard(volumeByMilkType: Map<String, Long>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThenaIcon(icon = ThenaIcons.Bottle, modifier = Modifier.size(ThenaTheme.spacing.lg))
                Text(
                    text = stringResource(R.string.feeding_stats_bottle),
                    style = ThenaTheme.typography.titleSmall,
                )
            }
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
            volumeByMilkType.forEach { (type, volume) ->
                val label = when (type) {
                    "BREAST_MILK" -> stringResource(R.string.feeding_stats_breast_milk)
                    "POWDERED_MILK" -> stringResource(R.string.feeding_stats_powdered_milk)
                    else -> type
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = label, style = ThenaTheme.typography.bodyMedium)
                    Text(
                        text = "${volume}ml",
                        style = ThenaTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyBreakdownSection(
    dailyBreakdown: PersistentList<DailyFeedingStatisticsInfo>,
    currentPageIndex: Int,
    onPageChanged: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = currentPageIndex.coerceIn(0, (dailyBreakdown.size - 1).coerceAtLeast(0)),
        pageCount = { dailyBreakdown.size },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page -> onPageChanged(page) }
    }

    Text(
        text = stringResource(R.string.feeding_stats_daily_breakdown),
        style = ThenaTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
    ) { page ->
        DailyStatPage(
            day = dailyBreakdown[page],
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = ThenaTheme.spacing.xs),
        )
    }

    if (dailyBreakdown.isNotEmpty()) {
        Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
        PagerIndicator(
            pageCount = dailyBreakdown.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DailyStatPage(
    day: DailyFeedingStatisticsInfo,
    modifier: Modifier = Modifier,
) {
    val primaryColor = ThenaTheme.colors.primary
    val secondaryColor = ThenaTheme.colors.secondary
    val onSurface = ThenaTheme.colors.onSurface

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ThenaTheme.spacing.md),
        ) {
            Text(
                text = day.date.toDisplayDate(),
                style = ThenaTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                DailyChip(
                    value = day.totalSessions.toString(),
                    label = stringResource(R.string.feeding_total)
                )
                DailyChip(
                    value = day.breastfeedingSessions.toString(),
                    label = stringResource(R.string.feeding_breast)
                )
                DailyChip(
                    value = day.bottleSessions.toString(),
                    label = stringResource(R.string.feeding_bottle)
                )
            }

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

            FeedingBarChart(
                breastSessions = day.breastfeedingSessions,
                bottleSessions = day.bottleSessions,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                labelColor = onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (day.totalBreastfeedingDurationSeconds > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ThenaIcon(
                                icon = ThenaIcons.Timer,
                                modifier = Modifier.size(ThenaTheme.spacing.md)
                            )
                            Text(
                                text = "${day.totalBreastfeedingDurationSeconds / 60} min",
                                style = ThenaTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (day.totalBottleVolumeMl > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ThenaIcon(icon = ThenaIcons.Bottle, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${day.totalBottleVolumeMl} ml",
                                style = ThenaTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyChip(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = ThenaTheme.typography.titleSmall)
        Text(text = label, style = ThenaTheme.typography.labelSmall)
    }
}

@Composable
private fun FeedingBarChart(
    breastSessions: Long,
    bottleSessions: Long,
    primaryColor: Color,
    secondaryColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val maxSessions = maxOf(breastSessions, bottleSessions, 1L).toFloat()
        val chartHeight = size.height * 0.75f
        val barWidth = size.width * 0.25f
        val gap = size.width * 0.1f
        val startX = (size.width - (barWidth * 2 + gap)) / 2f
        val cornerRadius = CornerRadius(8.dp.toPx())

        fun drawBar(sessions: Long, x: Float, color: Color) {
            val barHeight = (sessions / maxSessions) * chartHeight
            val top = chartHeight - barHeight
            drawRoundRect(
                color = color,
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius,
            )
        }

        drawBar(breastSessions, startX, primaryColor.copy(alpha = 0.8f))
        drawBar(bottleSessions, startX + barWidth + gap, secondaryColor.copy(alpha = 0.8f))

        // baseline
        drawLine(
            color = labelColor.copy(alpha = 0.2f),
            start = Offset(0f, chartHeight),
            end = Offset(size.width, chartHeight),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

