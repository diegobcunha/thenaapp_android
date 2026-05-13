package com.diegocunha.thenaapp.feature.feeding.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent

sealed interface FeedingStatisticsIntent : MviIntent {
    data class SelectPeriod(val period: FeedingStatsPeriod) : FeedingStatisticsIntent
    data class SelectCustomDateRange(val startMs: Long, val endMs: Long) : FeedingStatisticsIntent
    data object DismissDateRangePicker : FeedingStatisticsIntent
    data class PageChanged(val index: Int) : FeedingStatisticsIntent
}
