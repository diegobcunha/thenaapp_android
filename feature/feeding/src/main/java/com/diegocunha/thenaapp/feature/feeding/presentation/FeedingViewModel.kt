package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.BreastSegment
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.session.FeedingSessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FeedingViewModel(
    private val sessionManager: FeedingSessionManager,
) : BaseViewModel<FeedingState, FeedingIntent, FeedingEffect>(FeedingState()) {

    init {
        observeActiveSession()
        startTicker()
    }

    override fun processIntent(intent: FeedingIntent) {
        when (intent) {
            FeedingIntent.SelectBreastfeeding -> updateState { copy(feedingType = FeedingType.BREAST) }
            FeedingIntent.SelectBottle -> updateState { copy(feedingType = FeedingType.BOTTLE) }
            is FeedingIntent.TapBreast -> handleTapBreast(intent.breast)
            FeedingIntent.StopSession -> stopSession()
            is FeedingIntent.UpdateBottleMl -> updateState { copy(bottleMl = intent.ml) }
            is FeedingIntent.SelectBottleType -> updateState { copy(bottleType = intent.type) }
            FeedingIntent.SaveBottleFeeding -> saveBottleFeeding()
            FeedingIntent.Tick -> recalculateElapsed()
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionManager.activeSession.collectLatest { session ->
                session?.let {
                    updateState {
                        copy(
                            sessionId = session.sessionId,
                            feedingType = session.type,
                            activeBreast = session.activeBreast,
                        )
                    }
                    recalculateElapsed(session)
                }
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            sessionManager.tickerFlow.collect {
                sendIntent(FeedingIntent.Tick)
            }
        }
    }

    private fun handleTapBreast(breast: Breast) {
        val current = state.value
        viewModelScope.launch {
            when {
                current.sessionId == null -> sessionManager.startBreastfeeding(breast)
                current.activeBreast == breast -> sessionManager.pauseCurrentBreast()
                current.activeBreast != null -> sessionManager.switchBreast(breast)
                else -> sessionManager.resumeBreast(breast)
            }
        }
    }

    private fun stopSession() {
        viewModelScope.launch {
            sessionManager.finishSession()
            sendEffect(FeedingEffect.NavigateBack)
        }
    }

    private fun saveBottleFeeding() {
        val current = state.value
        val ml = current.bottleMl.trim().toIntOrNull()
        if (ml == null || ml <= 0) {
            sendEffect(FeedingEffect.ShowError(R.string.feeding_error_invalid_ml))
            return
        }
        val bottleType = current.bottleType ?: run {
            sendEffect(FeedingEffect.ShowError(R.string.feeding_error_bottle_type_required))
            return
        }
        viewModelScope.launch {
            sessionManager.startBottleFeeding(bottleType = bottleType, ml = ml)
            sendEffect(FeedingEffect.NavigateBack)
        }
    }

    private fun recalculateElapsed(session: ActiveFeedingSession? = sessionManager.activeSession.value) {
        session ?: return
        val now = System.currentTimeMillis()
        val leftElapsed = session.leftSegments.sumSegment(now)
        val rightElapsed = session.rightSegments.sumSegment(now)
        updateState {
            copy(
                leftElapsedSeconds = leftElapsed,
                rightElapsedSeconds = rightElapsed,
                totalElapsedSeconds = leftElapsed + rightElapsed,
            )
        }
    }

    private fun List<BreastSegment>.sumSegment(now: Long) = sumOf { seg ->
        val end = seg.endedAt ?: now
        (end - seg.startedAt) / ONE_SEC
    }

    companion object {
        private const val ONE_SEC = 1_000L
    }
}
