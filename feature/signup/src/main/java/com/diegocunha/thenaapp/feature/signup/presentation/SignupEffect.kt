package com.diegocunha.thenaapp.feature.signup.presentation

import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface SignupEffect : MviEffect {

    object NavigateToOnboarding: SignupEffect
}
