package com.diegocunha.thenaapp.sleep.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class SleepSessionUi(
    val id: String,
    val typeIcon: String,
    val typeName: String,
    val timeRange: String,
    val durationDisplay: String,
)