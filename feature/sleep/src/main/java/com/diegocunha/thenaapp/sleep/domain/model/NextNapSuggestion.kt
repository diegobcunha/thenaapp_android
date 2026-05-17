package com.diegocunha.thenaapp.sleep.domain.model

data class NextNapSuggestion(
    val suggestedNapStart: String,
    val windowOpen: String,
    val windowClose: String,
    val urgency: String,
    val wakeWindowMinutes: Int,
)
