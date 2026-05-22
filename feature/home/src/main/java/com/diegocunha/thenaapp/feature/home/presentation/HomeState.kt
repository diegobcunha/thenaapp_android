package com.diegocunha.thenaapp.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveFeedingInfo
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveSleepSessionInfo

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
    val babyId: String? = null,
    val babyPhotoUrl: String? = null,
    val babyName: String = "",
    val babyAge: BabyAge? = null,
    val babyInfo: BabyInfo? = null,
    @StringRes val error: Int? = null,
    val activeFeedingSession: ActiveFeedingInfo? = null,
    val feedingBannerElapsedSeconds: Long? = null,
    val todaySleepMinutes: Long? = null,
    val expectedSleepMinutes: Long? = null,
    val activeSleepSession: ActiveSleepSessionInfo? = null,
    val sleepBannerElapsedSeconds: Long? = null,
    val showCloseSessionPicker: Boolean = false,
    val isClosingSession: Boolean = false,
) : MviState
