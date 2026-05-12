package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.annotation.StringRes
import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface FeedingEffect : MviEffect {
    data object NavigateBack : FeedingEffect
    data class ShowError(@StringRes val message: Int) : FeedingEffect
}