package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.BreastSegment
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.session.FeedingSessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FeedingViewModel(
    private val sessionManager: FeedingSessionManager,
    private val repository: FeedingRepository,
    private val babyId: String,
) : BaseViewModel<FeedingState, FeedingIntent, FeedingEffect>(FeedingState()) {

    init {
        observeActiveSession()
        startTicker()
        loadTodayStats()
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
            FeedingIntent.UpdateDateTime -> updateState { copy(showStartTimePicker = true) }
            FeedingIntent.DismissStartTimePicker -> updateState { copy(showStartTimePicker = false) }
            is FeedingIntent.ConfirmStartTime -> onStartTimeConfirmed(intent.newStartedAtMs)
            is FeedingIntent.ConfirmBreastForTimeChange -> applyBreastStartTimeChange(intent.breast)
            FeedingIntent.DismissBreastPickerForTimeChange -> updateState {
                copy(showBreastPickerForTimeChange = false, pendingNewStartedAtMs = null)
            }
            FeedingIntent.Tick -> recalculateElapsed()
            FeedingIntent.OpenStatistics -> sendEffect(FeedingEffect.NavigateToStatistics)
        }
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            val result = repository.getStatistics(babyId)
            if (result is Resource.Success) {
                updateState { copy(todayStats = result.data) }
            }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionManager.activeSession.collectLatest { session ->
                if (session != null) {
                    updateState {
                        copy(
                            sessionId = session.sessionId,
                            feedingType = session.type,
                            activeBreast = session.activeBreast,
                            sessionStartedAt = session.startedAt,
                        )
                    }
                    recalculateElapsed(session)
                } else {
                    updateState {
                        copy(
                            sessionId = null,
                            feedingType = null,
                            activeBreast = null,
                            sessionStartedAt = null,
                            leftElapsedSeconds = 0L,
                            rightElapsedSeconds = 0L,
                            totalElapsedSeconds = 0L,
                        )
                    }
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
            runCatching {
                when {
                    current.sessionId == null -> sessionManager.startBreastfeeding(breast, babyId)
                    current.activeBreast == breast -> sessionManager.pauseCurrentBreast()
                    current.activeBreast != null -> sessionManager.switchBreast(breast)
                    else -> sessionManager.resumeBreast(breast)
                }
            }.onFailure {
                sendEffect(FeedingEffect.ShowError(R.string.feeding_error_network))
            }
        }
    }

    private fun stopSession() {
        viewModelScope.launch {
            runCatching {
                sessionManager.finishSession()
            }.onFailure {
                sendEffect(FeedingEffect.ShowError(R.string.feeding_error_network))
                return@launch
            }
            loadTodayStats()
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
            runCatching {
                sessionManager.startBottleFeeding(bottleType = bottleType, ml = ml, babyId = babyId)
            }.onFailure {
                sendEffect(FeedingEffect.ShowError(R.string.feeding_error_network))
                return@launch
            }
            updateState { copy(feedingType = null, bottleMl = "", bottleType = null) }
            loadTodayStats()
        }
    }

    private fun onStartTimeConfirmed(newStartedAtMs: Long) {
        if (newStartedAtMs >= System.currentTimeMillis()) {
            sendEffect(FeedingEffect.ShowError(R.string.feeding_error_start_time_future))
            return
        }
        updateState {
            copy(
                showStartTimePicker = false,
                showBreastPickerForTimeChange = true,
                pendingNewStartedAtMs = newStartedAtMs,
            )
        }
    }

    private fun applyBreastStartTimeChange(breast: Breast) {
        val pendingTime = state.value.pendingNewStartedAtMs ?: return
        updateState { copy(showBreastPickerForTimeChange = false, pendingNewStartedAtMs = null) }
        viewModelScope.launch {
            runCatching {
                sessionManager.updateBreastStartTime(breast, pendingTime)
                recalculateElapsed()
            }.onFailure { e ->
                val errorRes = if (e is IllegalArgumentException) {
                    R.string.feeding_error_start_time_overlap
                } else {
                    R.string.feeding_error_network
                }
                sendEffect(FeedingEffect.ShowError(errorRes))
            }
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