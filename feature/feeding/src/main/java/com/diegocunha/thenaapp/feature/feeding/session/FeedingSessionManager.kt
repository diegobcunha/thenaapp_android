package com.diegocunha.thenaapp.feature.feeding.session

import android.content.Context
import android.content.Intent
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.BreastSegment
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.service.FeedingTimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID

class FeedingSessionManager(
    private val repository: FeedingRepository,
    private val context: Context,
    dispatchersProvider: DispatchersProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.io())

    private val _activeSession = MutableStateFlow<ActiveFeedingSession?>(null)
    val activeSession: StateFlow<ActiveFeedingSession?> = _activeSession.asStateFlow()

    val tickerFlow: Flow<Unit> = flow {
        while (true) {
            delay(ONE_SEC)
            emit(Unit)
        }
    }

    companion object {
        private const val ONE_SEC = 1_000L
    }

    init {
        scope.launch { restoreSession() }
    }

    private suspend fun restoreSession() {
        val session = repository.getActiveSession() ?: return
        _activeSession.value = session
        startService()
    }

    suspend fun startBreastfeeding(breast: Breast) {
        val sessionId = UUID.randomUUID().toString()
        val segmentId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        repository.createBreastSession(sessionId = sessionId, babyId = "", startedAt = now)
        repository.createSegment(segmentId = segmentId, sessionId = sessionId, breast = breast, startedAt = now)
        _activeSession.value = ActiveFeedingSession(
            sessionId = sessionId,
            type = FeedingType.BREAST,
            startedAt = now,
            activeBreast = breast,
            leftSegments = if (breast == Breast.LEFT) {
                listOf(openSegment(segmentId, breast, now))
            } else {
                emptyList()
            },
            rightSegments = if (breast == Breast.RIGHT) {
                listOf(openSegment(segmentId, breast, now))
            } else {
                emptyList()
            }
        )
        startService()
    }

    suspend fun switchBreast(newBreast: Breast) {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        val activeSegmentId = repository.getActiveSegmentId(session.sessionId)
        if (activeSegmentId != null) {
            repository.closeSegment(segmentId = activeSegmentId, endedAt = now)
        }
        val newSegmentId = UUID.randomUUID().toString()
        repository.createSegment(segmentId = newSegmentId, sessionId = session.sessionId, breast = newBreast, startedAt = now)
        _activeSession.value = repository.getActiveSession()
    }

    suspend fun pauseCurrentBreast() {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        val activeSegmentId = repository.getActiveSegmentId(session.sessionId) ?: return
        repository.closeSegment(segmentId = activeSegmentId, endedAt = now)
        _activeSession.value = repository.getActiveSession()
    }

    suspend fun resumeBreast(breast: Breast) {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        val newSegmentId = UUID.randomUUID().toString()
        repository.createSegment(segmentId = newSegmentId, sessionId = session.sessionId, breast = breast, startedAt = now)
        _activeSession.value = repository.getActiveSession()
    }

    suspend fun finishSession() {
        val session = _activeSession.value ?: return
        val now = System.currentTimeMillis()
        val activeSegmentId = repository.getActiveSegmentId(session.sessionId)
        if (activeSegmentId != null) {
            repository.closeSegment(segmentId = activeSegmentId, endedAt = now)
        }
        repository.closeSession(sessionId = session.sessionId, endedAt = now)
        _activeSession.value = null
        stopService()
    }

    suspend fun startBottleFeeding(bottleType: BottleType, ml: Int) {
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        repository.createBottleSession(
            sessionId = sessionId,
            babyId = "",
            startedAt = now,
            ml = ml,
            bottleType = bottleType,
        )
    }

    private fun startService() {
        context.startForegroundService(Intent(context, FeedingTimerService::class.java))
    }

    private fun stopService() {
        context.stopService(Intent(context, FeedingTimerService::class.java))
    }

    private fun openSegment(id: String, breast: Breast, startedAt: Long) = BreastSegment(
        id = id,
        breast = breast,
        startedAt = startedAt,
        endedAt = null,
    )
}
