package com.diegocunha.thenaapp.feature.onboarding.presentation

import com.diegocunha.thenaapp.core.mvi.MviState

data class OnboardingState(
    val currentSlide: Int = 0,
) : MviState