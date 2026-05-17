package com.diegocunha.thenaapp.sleep.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList

@Immutable
data class SleepWeeklyStatsUi(
    val days: PersistentList<SleepDailyStatsUi>,
    val weeklyAvgDisplay: String?,
    val trend: String,
)
