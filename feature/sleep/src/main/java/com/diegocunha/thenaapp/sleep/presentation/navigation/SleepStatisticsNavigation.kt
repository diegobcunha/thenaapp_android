package com.diegocunha.thenaapp.sleep.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class SleepStatisticsNavigation(val babyId: String) : NavKey
