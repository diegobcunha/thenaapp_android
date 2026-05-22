package com.diegocunha.thenaapp.datasource.network.model.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    @SerialName("user_name")
    val userName: String,
    val baby: BabyHomeDto?,
    @SerialName("today_sleep_minutes")
    val todaySleepMinutes: Long?,
    @SerialName("expected_sleep_minutes")
    val expectedSleepMinutes: Long?,
    @SerialName("active_sleep_session")
    val activeSleepSession: ActiveSleepSessionDto? = null,
)
