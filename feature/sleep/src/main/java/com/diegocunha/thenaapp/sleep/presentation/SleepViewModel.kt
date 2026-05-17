package com.diegocunha.thenaapp.sleep.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.domain.model.ActiveSleepSession
import com.diegocunha.thenaapp.sleep.session.SleepSessionManager
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepViewModel(
    private val sessionManager: SleepSessionManager,
    private val repository: SleepRepository,
    private val babyId: String,
) : BaseSleepViewModel<SleepViewState, SleepIntent, SleepEffect>(SleepViewState()) {

    init {
        sessionManager.restoreSession(babyId)
        observeActiveSession()
        startTicker()
        loadTodayData()
    }

    override fun processIntent(intent: SleepIntent) {
        when (intent) {
            SleepIntent.StartSleep -> startSleep()
            SleepIntent.StopSleep -> stopSleep()
            is SleepIntent.SelectSleepType -> updateState { copy(activeSleepType = intent.sleepType) }
            is SleepIntent.LogPastSleep -> logPastSleep(intent.startMs, intent.endMs)
            SleepIntent.ShowLogDialog -> updateState { copy(showLogDialog = true) }
            SleepIntent.DismissLogDialog -> updateState { copy(showLogDialog = false) }
            SleepIntent.Tick -> recalculateElapsed()
            SleepIntent.OpenStatistics -> sendEffect(SleepEffect.NavigateToStatistics)
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionManager.activeSession.collectLatest { session ->
                if (session != null) {
                    updateState {
                        copy(
                            isRunning = true,
                            activeSessionId = session.id,
                            activeSleepType = session.sleepType,
                        )
                    }
                    recalculateElapsed(session)
                } else {
                    updateState {
                        copy(
                            isRunning = false,
                            activeSessionId = null,
                            elapsedSeconds = 0L,
                        )
                    }
                }
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            sessionManager.tickerFlow.collect {
                sendIntent(SleepIntent.Tick)
            }
        }
    }

    private fun startSleep() {
        viewModelScope.launch {
            runCatching {
                sessionManager.startSession(babyId, state.value.activeSleepType)
            }.onFailure {
                sendEffect(SleepEffect.ShowError(R.string.sleep_error_network))
            }
        }
    }

    private fun stopSleep() {
        viewModelScope.launch {
            runCatching {
                sessionManager.stopSession(babyId)
            }.onFailure {
                sendEffect(SleepEffect.ShowError(R.string.sleep_error_network))
                return@launch
            }
            loadTodayData()
        }
    }

    private fun logPastSleep(startMs: Long, endMs: Long) {
        if (endMs <= startMs) {
            sendEffect(SleepEffect.ShowError(R.string.sleep_error_end_before_start))
            return
        }
        updateState { copy(showLogDialog = false) }
        viewModelScope.launch {
            when (val result = repository.logPastSession(babyId, state.value.activeSleepType, startMs, endMs)) {
                is Resource.Error -> sendEffect(SleepEffect.ShowError(R.string.sleep_error_network))
                is Resource.Success -> {
                    updateState { copy(sessions = (listOf(result.data.toUi()) + sessions).toPersistentList()) }
                    refreshDailyStats()
                }
                else -> Unit
            }
        }
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            val today = todayString()
            val statsResult = repository.getDailyStats(babyId, today)
            val sessionsResult = repository.listSessions(babyId, today)
            if (statsResult is Resource.Success) {
                updateState { copy(todayStats = statsResult.data.toUi()) }
            }
            if (sessionsResult is Resource.Success) {
                updateState { copy(sessions = sessionsResult.data.map { it.toUi() }.toPersistentList()) }
            }
        }
    }

    private fun refreshDailyStats() {
        viewModelScope.launch {
            val result = repository.getDailyStats(babyId, todayString())
            if (result is Resource.Success) {
                updateState { copy(todayStats = result.data.toUi()) }
            }
        }
    }

    private fun recalculateElapsed(session: ActiveSleepSession? = sessionManager.activeSession.value) {
        session ?: return
        val elapsed = (System.currentTimeMillis() - session.startTimeMs) / 1_000L
        updateState { copy(elapsedSeconds = elapsed) }
    }

    private fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
