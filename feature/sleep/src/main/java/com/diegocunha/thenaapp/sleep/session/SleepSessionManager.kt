package com.diegocunha.thenaapp.sleep.session

import android.content.Context
import android.content.Intent
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.domain.model.ActiveSleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.service.SleepTimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SleepSessionManager(
    private val repository: SleepRepository,
    private val context: Context,
    dispatchersProvider: DispatchersProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.io())

    private val _activeSession = MutableStateFlow<ActiveSleepSession?>(null)
    val activeSession: StateFlow<ActiveSleepSession?> = _activeSession.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tickerFlow: Flow<Unit> = activeSession.flatMapLatest { session ->
        if (session != null) {
            flow {
                while (true) {
                    delay(ONE_SEC)
                    emit(Unit)
                }
            }
        } else {
            emptyFlow()
        }
    }

    fun restoreSession(babyId: String) {
        scope.launch {
            val session = repository.getActiveSession(babyId) ?: return@launch
            _activeSession.value = session
            startService()
        }
    }

    suspend fun startSession(babyId: String, sleepType: SleepType): ActiveSleepSession {
        val now = System.currentTimeMillis()
        return when (val result = repository.startSession(babyId, sleepType, now)) {
            is Resource.Success -> {
                val session = ActiveSleepSession(
                    id = result.data.id,
                    startTimeMs = result.data.startTimeMs,
                    sleepType = result.data.sleepType,
                )
                _activeSession.value = session
                startService()
                session
            }
            is Resource.Error -> throw result.exception
            else -> throw IllegalStateException("Unexpected loading state")
        }
    }

    suspend fun stopSession(babyId: String) {
        val session = _activeSession.value ?: return
        when (val result = repository.endSession(babyId, session.id, System.currentTimeMillis())) {
            is Resource.Error -> throw result.exception
            else -> Unit
        }
        _activeSession.value = null
        stopService()
    }

    private fun startService() {
        context.startForegroundService(Intent(context, SleepTimerService::class.java))
    }

    private fun stopService() {
        context.stopService(Intent(context, SleepTimerService::class.java))
    }

    companion object {
        private const val ONE_SEC = 1_000L
    }
}
