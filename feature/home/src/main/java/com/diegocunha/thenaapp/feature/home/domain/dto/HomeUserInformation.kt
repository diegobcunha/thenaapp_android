package com.diegocunha.thenaapp.feature.home.domain.dto

import java.math.BigDecimal

data class HomeUserInformation(
    val userName: String,
    val babyInformation: HomeBabyInformation,
    val todaySleepMinutes: Long?,
    val expectedSleepMinutes: Long?,
    val activeSleepSession: ActiveSleepSessionInfo? = null,
)

data class HomeBabyInformation(
    val babyId: String,
    val babyName: String,
    val babyBirthDate: String,
    val babyWeight: BigDecimal,
    val babyHeight: BigDecimal,
    val babyPhotoUrl: String? = null,
)

data class ActiveSleepSessionInfo(
    val id: String,
    val startTimeMs: Long,
    val sleepType: String,
) {
    fun elapsedSeconds(): Long = (System.currentTimeMillis() - startTimeMs) / 1_000L
}
