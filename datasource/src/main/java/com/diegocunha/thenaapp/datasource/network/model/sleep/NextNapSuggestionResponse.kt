package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NextNapSuggestionResponse(
    @SerialName("suggested_nap_start") val suggestedNapStart: String,
    @SerialName("window_open") val windowOpen: String,
    @SerialName("window_close") val windowClose: String,
    @SerialName("urgency") val urgency: String,
    @SerialName("wake_window_minutes") val wakeWindowMinutes: Int,
)
