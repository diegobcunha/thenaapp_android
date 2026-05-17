package com.diegocunha.thenaapp.sleep.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class SleepDailyStatsUi(
    val date: String,
    val progress: Float,
    val totalDisplay: String,
    val goalDisplay: String,
    val sessionCount: Int,
    val efficiencyDisplay: String,
    val totalSleepMinutes: Long,
    val insight: String?,
)