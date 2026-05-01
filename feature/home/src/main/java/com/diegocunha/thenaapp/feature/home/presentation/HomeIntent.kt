package com.diegocunha.thenaapp.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.datasource.database.ActiveFeedingSnapshot

sealed interface HomeIntent : MviIntent {

    object EditBabyInfo : HomeIntent
    object UserProfile : HomeIntent
    object SleepInfo : HomeIntent
    object FeedInfo: HomeIntent
    object VaccineInfo : HomeIntent
    object SummaryInfo: HomeIntent
}

@Immutable
data class BabyAge(
    val totalMonths: Int,
    val years: Int,
    val remainderMonths: Int,
)

@Immutable
data class BabyInfo(
    val height: String,
    val weight: String,
)

@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val babyPhotoUrl: String? = null,
    val babyName: String = "",
    val babyAge: BabyAge? = null,
    val babyInfo: BabyInfo? = null,
    @StringRes val error: Int? = null,
    val activeFeedingSession: ActiveFeedingSnapshot? = null,
    val feedingBannerElapsedSeconds: Long = 0L,
) : MviState
