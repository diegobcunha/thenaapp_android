package com.diegocunha.thenaapp.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.coreui.R
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val homeRepository: HomeRepository,
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
            HomeIntent.SleepInfo,
            HomeIntent.SummaryInfo,
            HomeIntent.VaccineInfo -> sendEffect(HomeEffect.NotDevelopedYet)
            HomeIntent.FeedInfo -> sendEffect(HomeEffect.NavigateToFeeding)
        }
    }

    private fun observeActiveFeeding() {
        viewModelScope.launch {
            homeRepository.observeActiveFeeding().collectLatest { snapshot ->
                updateState { copy(activeFeedingSession = snapshot) }
            }
        }
    }

    private fun startFeedingTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val session = state.value.activeFeedingSession ?: continue
                val elapsed = (System.currentTimeMillis() - session.startedAt) / 1_000L
                updateState { copy(feedingBannerElapsedSeconds = elapsed) }
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            when (val result = homeRepository.getUserInformation()) {
                is Resource.Success -> updateState {
                    val data = result.data
                    val baby = data.babyInformation
                    copy(
                        isLoading = false,
                        userName = data.userName,
                        babyPhotoUrl = baby.babyPhotoUrl,
                        babyName = baby.babyName,
                        babyAge = calculateBabyAge(baby.babyBirthDate),
                        babyInfo = BabyInfo(
                            height = baby.babyHeight.toString(),
                            weight = baby.babyWeight.toString(),
                        )
                    )
                }

                is Resource.Error -> updateState {
                    copy(
                        isLoading = false,
                        error = R.string.generic_error
                    )
                }

                else -> Unit
            }
        }
    }

    private fun calculateBabyAge(birthDateString: String): BabyAge? {
        return try {
            val parts = birthDateString.split("-")
            val birth = Calendar.getInstance().apply {
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
            val now = Calendar.getInstance()

            var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)

            if (months < 0) {
                years--
                months += 12
            }

            BabyAge(
                totalMonths = years * 12 + months,
                years = years,
                remainderMonths = months,
            )
        } catch (_: Exception) {
            null
        }
    }
}
