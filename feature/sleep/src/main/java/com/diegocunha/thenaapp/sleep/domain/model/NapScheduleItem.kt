package com.diegocunha.thenaapp.sleep.domain.model

data class NapScheduleItem(
    val startTime: String,
    val durationMinutes: Int,
    val sleepType: SleepType,
)
