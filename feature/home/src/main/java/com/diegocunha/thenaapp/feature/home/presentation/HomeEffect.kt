package com.diegocunha.thenaapp.feature.home.presentation

import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface HomeEffect : MviEffect {

    object NotDevelopedYet : HomeEffect

}
