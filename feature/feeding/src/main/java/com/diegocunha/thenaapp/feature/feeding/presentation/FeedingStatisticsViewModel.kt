package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.DailyFeedingStatistics
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingStatistics
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FeedingStatisticsViewModel(
    private val repository: FeedingRepository,
    private val babyId: String,
) : BaseViewModel<FeedingStatisticsState, FeedingStatisticsIntent, FeedingStatisticsEffect>(
    initialState = FeedingStatisticsState(isLoading = true),
) {

    init {
        loadStatistics()
    }

    override fun processIntent(intent: FeedingStatisticsIntent) {
        when (intent) {
            is FeedingStatisticsIntent.SelectPeriod -> {
                val selectedPeriod = intent.period
                if (selectedPeriod == FeedingStatsPeriod.CUSTOM) {
                    updateState {
                        copy(
                            selectedPeriod = FeedingStatsPeriod.CUSTOM,
                            showDateRangePicker = true
                        )
                    }
                } else {
                    updateState { copy(selectedPeriod = selectedPeriod, currentPageIndex = 0) }
                    loadStatistics()
                }
            }

            is FeedingStatisticsIntent.SelectCustomDateRange -> {
                val start = intent.startMs.toDateString()
                val end = intent.endMs.toDateString()
                updateState {
                    copy(
                        customStartDate = start,
                        customEndDate = end,
                        showDateRangePicker = false,
                        currentPageIndex = 0,
                    )
                }
                loadStatistics()
            }

            FeedingStatisticsIntent.DismissDateRangePicker -> {
                updateState { copy(showDateRangePicker = false) }
            }

            is FeedingStatisticsIntent.PageChanged -> {
                updateState { copy(currentPageIndex = intent.index) }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val (date, startDate, endDate) = state.value.toApiParams()
            when (val result = repository.getStatistics(babyId, date, startDate, endDate)) {
                is Resource.Success -> updateState {
                    copy(statistics = result.data.toState(), isLoading = false, currentPageIndex = 0)
                }

                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(FeedingStatisticsEffect.ShowError(R.string.feeding_error_network))
                }

                Resource.Loading -> Unit
            }
        }
    }

    private fun FeedingStatisticsState.toApiParams(): Triple<String?, String?, String?> {
        val today = todayString()
        return when (selectedPeriod) {
            FeedingStatsPeriod.TODAY -> Triple(today, null, null)
            FeedingStatsPeriod.WEEK -> Triple(null, daysAgoString(6), today)
            FeedingStatsPeriod.MONTH -> Triple(null, daysAgoString(29), today)
            FeedingStatsPeriod.CUSTOM -> Triple(null, customStartDate, customEndDate)
        }
    }

    private fun todayString(): String = localDateFormatter().format(Date())

    private fun daysAgoString(days: Int): String =
        localDateFormatter().format(Date(System.currentTimeMillis() - days * DAY_MS))

    // MaterialDateRangePicker returns UTC-midnight timestamps, so keep UTC here.
    private fun Long.toDateString(): String = utcDateFormatter().format(Date(this))

    private fun localDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun utcDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun FeedingStatistics.toState() = FeedingStatisticsInfo(
        periodStart = periodStart,
        periodEnd = periodEnd,
        totalSessions = totalSessions,
        breastfeedingSessions = breastfeedingSessions,
        bottleSessions = bottleSessions,
        totalBreastfeedingDurationSeconds = totalBreastfeedingDurationSeconds,
        averageBottleVolumeMl = averageBottleVolumeMl,
        dailyBreakdown = dailyBreakdown.map { it.toState() }.toPersistentList(),
        averageBreastfeedingDurationSeconds = averageBreastfeedingDurationSeconds,
        volumeByMilkType = volumeByMilkType.toPersistentMap(),
        totalBottleVolumeMl = totalBottleVolumeMl
    )

    private fun DailyFeedingStatistics.toState() = DailyFeedingStatisticsInfo(
        date = date,
        totalSessions = totalSessions,
        breastfeedingSessions = breastfeedingSessions,
        bottleSessions = bottleSessions,
        totalBreastfeedingDurationSeconds = totalBreastfeedingDurationSeconds,
        totalBottleVolumeMl = totalBottleVolumeMl,
    )

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
