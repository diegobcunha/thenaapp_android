package com.diegocunha.thenaapp.feature.onboarding.domain.repository

interface OnboardingRepository {
    fun hasSeenOnboarding(): Boolean
    fun markOnboardingSeen()
}