package com.diegocunha.thenaapp.feature.baby.presentation.create

import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface CreateBabyEffect : MviEffect {
    object NavigateToHome : CreateBabyEffect
}
