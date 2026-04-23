package com.diegocunha.thenaapp.feature.onboarding.presentation

import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
) : BaseViewModel<OnboardingState, OnboardingIntent, OnboardingEffect>(OnboardingState()) {

    override fun processIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageChanged -> updateState { copy(currentSlide = intent.page) }
            OnboardingIntent.Skip -> finishOnboarding()
            OnboardingIntent.Done -> finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        onboardingRepository.markOnboardingSeen()
        sendEffect(OnboardingEffect.NavigateToLogin)
    }
}