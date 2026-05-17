package com.diegocunha.thenaapp.sleep.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepStatisticsViewModel(
    private val repository: SleepRepository,
    private val babyId: String,
) : BaseSleepViewModel<SleepStatisticsState, SleepStatisticsIntent, SleepStatisticsEffect>(
    initialState = SleepStatisticsState(isLoading = true),
) {

    init {
        loadStatistics()
    }

    override fun processIntent(intent: SleepStatisticsIntent) {
        when (intent) {
            is SleepStatisticsIntent.SelectPeriod -> {
                updateState { copy(selectedPeriod = intent.period, currentPageIndex = 0) }
                loadStatistics()
            }
            is SleepStatisticsIntent.PageChanged -> {
                updateState { copy(currentPageIndex = intent.index) }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            when (state.value.selectedPeriod) {
                SleepStatsPeriod.TODAY -> loadDailyStats(todayString())
                SleepStatsPeriod.WEEK -> loadWeeklyStats(daysAgoString(6))
                SleepStatsPeriod.MONTH -> loadDailyStats(todayString())
            }
        }
    }

    private suspend fun loadDailyStats(date: String) {
        when (val result = repository.getDailyStats(babyId, date)) {
            is Resource.Success -> updateState { copy(dailyStats = result.data.toUi(), isLoading = false) }
            is Resource.Error -> {
                updateState { copy(isLoading = false) }
                sendEffect(SleepStatisticsEffect.ShowError(R.string.sleep_error_network))
            }
            else -> Unit
        }
    }

    private suspend fun loadWeeklyStats(weekStart: String) {
        when (val result = repository.getWeeklyStats(babyId, weekStart)) {
            is Resource.Success -> updateState { copy(weeklyStats = result.data.toUi(), isLoading = false) }
            is Resource.Error -> {
                updateState { copy(isLoading = false) }
                sendEffect(SleepStatisticsEffect.ShowError(R.string.sleep_error_network))
            }
            else -> Unit
        }
    }

    private fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun daysAgoString(days: Int): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - days * DAY_MS))

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
