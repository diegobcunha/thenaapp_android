package com.diegocunha.thenaapp.sleep.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviEffect
import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.sleep.presentation.model.SleepDailyStatsUi
import com.diegocunha.thenaapp.sleep.presentation.model.SleepWeeklyStatsUi

@Immutable
data class SleepStatisticsState(
    val selectedPeriod: SleepStatsPeriod = SleepStatsPeriod.TODAY,
    val isLoading: Boolean = false,
    val dailyStats: SleepDailyStatsUi? = null,
    val weeklyStats: SleepWeeklyStatsUi? = null,
    val currentPageIndex: Int = 0,
) : MviState

sealed interface SleepStatisticsIntent : MviIntent {
    data class SelectPeriod(val period: SleepStatsPeriod) : SleepStatisticsIntent
    data class PageChanged(val index: Int) : SleepStatisticsIntent
}

sealed interface SleepStatisticsEffect : MviEffect {
    data class ShowError(@StringRes val message: Int) : SleepStatisticsEffect
}
