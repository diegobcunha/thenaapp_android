package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

@Immutable
data class FeedingStatisticsState(
    val statistics: FeedingStatisticsInfo? = null,
    val isLoading: Boolean = false,
    val selectedPeriod: FeedingStatsPeriod = FeedingStatsPeriod.TODAY,
    val showDateRangePicker: Boolean = false,
    val customStartDate: String? = null,
    val customEndDate: String? = null,
    val currentPageIndex: Int = 0,
) : MviState

@Immutable
data class FeedingStatisticsInfo(
    val periodStart: String,
    val periodEnd: String,
    val totalSessions: Long,
    val breastfeedingSessions: Long,
    val bottleSessions: Long,
    val totalBreastfeedingDurationSeconds: Long,
    val averageBreastfeedingDurationSeconds: Long,
    val totalBottleVolumeMl: Long,
    val averageBottleVolumeMl: Long,
    val volumeByMilkType: PersistentMap<String, Long>,
    val dailyBreakdown: PersistentList<DailyFeedingStatisticsInfo>,
)

@Immutable
data class DailyFeedingStatisticsInfo(
    val date: String,
    val totalSessions: Long,
    val breastfeedingSessions: Long,
    val bottleSessions: Long,
    val totalBreastfeedingDurationSeconds: Long,
    val totalBottleVolumeMl: Long,
)
