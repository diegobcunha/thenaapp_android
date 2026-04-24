package com.diegocunha.thenaapp.di

import com.diegocunha.thenaapp.presentation.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        MainViewModel(
            onboardingRepository = get(),
            firebaseAuth = get(),
        )
    }
}
