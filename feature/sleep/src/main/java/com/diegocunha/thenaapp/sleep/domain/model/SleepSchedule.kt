package com.diegocunha.thenaapp.sleep.domain.model

data class SleepSchedule(
    val date: String,
    val naps: List<NapScheduleItem>,
    val nightSleepMinutes: Int,
    val totalSleepMinutes: Int,
)
