package com.diegocunha.thenaapp.feature.onboarding.di

import com.diegocunha.thenaapp.feature.onboarding.data.OnboardingRepositoryImpl
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {

    single<OnboardingRepository> {
        OnboardingRepositoryImpl(
            customSharedPreferences = get(),
        )
    }

    viewModel {
        OnboardingViewModel(
            onboardingRepository = get(),
        )
    }
}
