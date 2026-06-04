package com.diegocunha.thenaapp.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.coreui.R
import com.diegocunha.thenaapp.feature.home.domain.BabyAgeResult
import com.diegocunha.thenaapp.feature.home.domain.CalculateBabyAgeUseCase
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveFeedingInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val calculateBabyAge: CalculateBabyAgeUseCase,
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState(isLoading = true)) {

    init {
        loadContent()
        observeActiveFeedingWithTick()
        startSleepTicker()
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.EditBabyInfo,
            HomeIntent.UserProfile,
            HomeIntent.SummaryInfo -> sendEffect(HomeEffect.NotDevelopedYet)

            HomeIntent.VaccineInfo -> {
                val babyId = state.value.babyId ?: return
                sendEffect(HomeEffect.NavigateToVaccine(babyId))
            }

            HomeIntent.FeedInfo -> {
                val babyId = state.value.babyId ?: return
                sendEffect(HomeEffect.NavigateToFeeding(babyId))
            }

            HomeIntent.SleepInfo -> {
                val babyId = state.value.babyId ?: return
                sendEffect(HomeEffect.NavigateToSleep(babyId))
            }

            HomeIntent.ActiveSleepBannerTapped ->
                updateState { copy(showCloseSessionPicker = true) }

            HomeIntent.DismissCloseSessionPicker ->
                updateState { copy(showCloseSessionPicker = false) }

            is HomeIntent.CloseSleepSession -> closeSession(intent.endTimeMs)
        }
    }

    private fun observeActiveFeedingWithTick() {
        viewModelScope.launch {
            homeRepository.observeActiveFeeding()
                .flatMapLatest { session ->
                    if (session == null) {
                        flowOf<Pair<ActiveFeedingInfo?, Long?>>(null to null)
                    } else {
                        flow {
                            while (true) {
                                emit(session to session.elapsedSeconds())
                                delay(ONE_SEC)
                            }
                        }
                    }
                }
                .collectLatest { (session, elapsed) ->
                    updateState {
                        copy(activeFeedingSession = session, feedingBannerElapsedSeconds = elapsed)
                    }
                }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            when (val result = homeRepository.getHomeData()) {
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
                            todaySleepMinutes = data.todaySleepMinutes,
                            expectedSleepMinutes = data.expectedSleepMinutes,
                            activeSleepSession = data.activeSleepSession,
                            sleepBannerElapsedSeconds = data.activeSleepSession?.elapsedSeconds(),
                        )
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

    private fun closeSession(endTimeMs: Long) {
        val babyId = state.value.babyId ?: return
        val sessionId = state.value.activeSleepSession?.id ?: return
        viewModelScope.launch {
            updateState { copy(isClosingSession = true) }
            when (homeRepository.closeSleepSession(babyId, sessionId, endTimeMs)) {
                is Resource.Success -> {
                    updateState {
                        copy(
                            activeSleepSession = null,
                            sleepBannerElapsedSeconds = null,
                            showCloseSessionPicker = false,
                            isClosingSession = false,
                        )
                    }
                    sendEffect(HomeEffect.SleepSessionClosed)
                }
                is Resource.Error -> {
                    updateState { copy(isClosingSession = false) }
                    sendEffect(HomeEffect.CloseSessionError)
                }
                else -> Unit
            }
        }
    }

    private fun startSleepTicker() {
        viewModelScope.launch {
            state
                .map { it.activeSleepSession }
                .distinctUntilChanged()
                .flatMapLatest { session ->
                    if (session == null) emptyFlow()
                    else flow {
                        while (true) {
                            delay(ONE_SEC)
                            emit(session.elapsedSeconds())
                        }
                    }
                }
                .collectLatest { elapsed ->
                    updateState { copy(sleepBannerElapsedSeconds = elapsed) }
                }
        }
    }

    private fun BabyAgeResult.toPresentation() = BabyAge(
        totalMonths = totalMonths,
        years = years,
        remainderMonths = remainderMonths,
    )

    companion object {
        private const val ONE_SEC = 1_000L
    }
}
