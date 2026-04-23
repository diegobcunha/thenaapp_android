package com.diegocunha.thenaapp

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.navigation.OnboardingNavigation

class MainViewModel(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    fun getStartDestination(): NavKey {
        return if (onboardingRepository.hasSeenOnboarding()) {
            LoginNavigation
        } else {
            OnboardingNavigation
        }
    }
}
