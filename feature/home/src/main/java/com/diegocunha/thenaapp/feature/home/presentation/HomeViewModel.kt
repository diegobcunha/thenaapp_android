package com.diegocunha.thenaapp.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.coreui.R
import com.diegocunha.thenaapp.datasource.database.model.ActiveFeedingSnapshot
import com.diegocunha.thenaapp.feature.home.domain.BabyAgeResult
import com.diegocunha.thenaapp.feature.home.domain.CalculateBabyAgeUseCase
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val calculateBabyAge: CalculateBabyAgeUseCase,
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState(isLoading = true)) {

    init {
        loadContent()
        observeActiveFeeding()
        startFeedingTicker()
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.EditBabyInfo,
            HomeIntent.UserProfile,
            HomeIntent.SummaryInfo,
            HomeIntent.VaccineInfo -> sendEffect(HomeEffect.NotDevelopedYet)

            HomeIntent.FeedInfo -> {
                val babyId = state.value.babyId ?: return
                sendEffect(HomeEffect.NavigateToFeeding(babyId))
            }

            HomeIntent.SleepInfo -> {
                val babyId = state.value.babyId ?: return
                sendEffect(HomeEffect.NavigateToSleep(babyId))
            }
        }
    }

    private fun observeActiveFeeding() {
        viewModelScope.launch {
            homeRepository.observeActiveFeeding().collectLatest { snapshot ->
                updateState {
                    copy(
                        activeFeedingSession = snapshot,
                        feedingBannerElapsedSeconds = snapshot?.elapsedSeconds(),
                    )
                }
            }
        }
    }

    private fun startFeedingTicker() {
        viewModelScope.launch {
            state
                .map { it.activeFeedingSession?.activeSegmentStartedAt != null }
                .distinctUntilChanged()
                .collectLatest { isSegmentActive ->
                    if (!isSegmentActive) return@collectLatest
                    while (true) {
                        delay(1_000L)
                        val session = state.value.activeFeedingSession ?: break
                        if (session.activeSegmentStartedAt == null) break
                        updateState { copy(feedingBannerElapsedSeconds = session.elapsedSeconds()) }
                    }
                }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            when (val result = homeRepository.getUserInformation()) {
                is Resource.Success -> {
                    val data = result.data
                    val baby = data.babyInformation
                    updateState {
                        copy(
                            isLoading = false,
                            userName = data.userName,
                            babyId = baby.babyId,
                            babyPhotoUrl = baby.babyPhotoUrl,
                            babyName = baby.babyName,
                            babyAge = calculateBabyAge(baby.babyBirthDate)?.toPresentation(),
                            babyInfo = BabyInfo(
                                height = baby.babyHeight.toString(),
                                weight = baby.babyWeight.toString(),
                            ),
                        )
                    }
                    val sleepResult = homeRepository.getTodaySleepMinutes(baby.babyId)
                    if (sleepResult is Resource.Success) {
                        updateState { copy(todaySleepMinutes = sleepResult.data) }
                    }
                }

                is Resource.Error -> updateState {
                    copy(
                        isLoading = false,
                        error = R.string.generic_error,
                    )
                }

                else -> Unit
            }
        }
    }

    private fun BabyAgeResult.toPresentation() = BabyAge(
        totalMonths = totalMonths,
        years = years,
        remainderMonths = remainderMonths,
    )

    private fun ActiveFeedingSnapshot.elapsedSeconds(): Long {
        val segStart = activeSegmentStartedAt
        return if (segStart != null) {
            (closedSegmentsTotalMs + System.currentTimeMillis() - segStart) / 1_000L
        } else {
            closedSegmentsTotalMs / ONE_SEC
        }
    }

    companion object {
        private const val ONE_SEC = 1_000L
    }
}
