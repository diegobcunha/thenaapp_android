package com.diegocunha.thenaapp.sleep.presentation

import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.presentation.model.SleepDailyStatsUi
import com.diegocunha.thenaapp.sleep.presentation.model.SleepSessionUi
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SleepViewState(
    val isRunning: Boolean = false,
    val activeSessionId: String? = null,
    val elapsedSeconds: Long = 0L,
    val activeSleepType: SleepType = SleepType.NAP,
    val todayStats: SleepDailyStatsUi? = null,
    val sessions: PersistentList<SleepSessionUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val showLogDialog: Boolean = false,
) : MviState
