package com.diegocunha.thenaapp.sleep.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.sleep.domain.model.SleepType

sealed interface SleepIntent : MviIntent {
    data object StartSleep : SleepIntent
    data object StopSleep : SleepIntent
    data class SelectSleepType(val sleepType: SleepType) : SleepIntent
    data class LogPastSleep(val startMs: Long, val endMs: Long) : SleepIntent
    data object ShowLogDialog : SleepIntent
    data object DismissLogDialog : SleepIntent
    data object Tick : SleepIntent
    data object OpenStatistics : SleepIntent
}
