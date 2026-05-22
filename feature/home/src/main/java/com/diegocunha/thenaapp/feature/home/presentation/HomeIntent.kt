package com.diegocunha.thenaapp.feature.home.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent

sealed interface HomeIntent : MviIntent {

    object EditBabyInfo : HomeIntent
    object UserProfile : HomeIntent
    object SleepInfo : HomeIntent
    object FeedInfo: HomeIntent
    object VaccineInfo : HomeIntent
    object SummaryInfo: HomeIntent
    object ActiveSleepBannerTapped : HomeIntent
    object DismissCloseSessionPicker : HomeIntent
    data class CloseSleepSession(val endTimeMs: Long) : HomeIntent
}
