package com.diegocunha.thenaapp.feature.feeding.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class FeedingStatisticsNavigation(val babyId: String) : NavKey