package com.diegocunha.thenaapp.feature.onboarding.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent

sealed interface OnboardingIntent : MviIntent {
    data class PageChanged(val page: Int) : OnboardingIntent
    data object Skip : OnboardingIntent
    data object Done : OnboardingIntent
}