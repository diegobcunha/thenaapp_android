package com.diegocunha.thenaapp.sleep.domain.model

data class SleepSession(
    val id: String,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val durationMinutes: Long?,
    val sleepType: SleepType,
    val isActive: Boolean,
)
