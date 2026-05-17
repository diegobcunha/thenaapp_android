package com.diegocunha.thenaapp.sleep.presentation

import androidx.annotation.StringRes
import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface SleepEffect : MviEffect {
    data object NavigateToStatistics : SleepEffect
    data class ShowError(@StringRes val message: Int) : SleepEffect
}
