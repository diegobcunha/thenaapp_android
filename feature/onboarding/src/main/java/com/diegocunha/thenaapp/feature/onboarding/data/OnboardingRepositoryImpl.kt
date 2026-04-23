package com.diegocunha.thenaapp.feature.onboarding.data

import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository


internal class OnboardingRepositoryImpl(
    private val customSharedPreferences: CustomSharedPreferences,
) : OnboardingRepository {

    override fun hasSeenOnboarding(): Boolean =
        customSharedPreferences.getBoolean(KEY_HAS_SEEN, false)

    override fun markOnboardingSeen() {
        customSharedPreferences.putBoolean(KEY_HAS_SEEN, true)
    }

    private companion object {
        private const val KEY_HAS_SEEN = "has_seen_onboarding"
    }
}
