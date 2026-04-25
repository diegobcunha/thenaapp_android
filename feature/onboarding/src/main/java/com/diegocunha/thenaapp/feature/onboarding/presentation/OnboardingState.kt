package com.diegocunha.thenaapp.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState

@Immutable
data class OnboardingState(
    val currentSlide: Int = 0,
) : MviState