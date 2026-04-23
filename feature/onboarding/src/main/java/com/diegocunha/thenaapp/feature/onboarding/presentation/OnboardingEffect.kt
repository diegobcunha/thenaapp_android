package com.diegocunha.thenaapp.feature.onboarding.presentation

import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface OnboardingEffect : MviEffect {
    data object NavigateToLogin : OnboardingEffect
}