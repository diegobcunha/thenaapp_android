package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.annotation.StringRes
import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface FeedingStatisticsEffect : MviEffect {
    data class ShowError(@StringRes val message: Int) : FeedingStatisticsEffect
}
