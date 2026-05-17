package com.diegocunha.thenaapp.sleep.domain.model

data class ActiveSleepSession(
    val id: String,
    val startTimeMs: Long,
    val sleepType: SleepType,
)
