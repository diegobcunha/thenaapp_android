package com.diegocunha.thenaapp.feature.home.di

import com.diegocunha.thenaapp.feature.home.domain.CalculateBabyAgeUseCase
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.presentation.HomeViewModel
import com.diegocunha.thenaapp.feature.home.repository.HomeRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel {
        HomeViewModel(
            homeRepository = get(),
            calculateBabyAge = get(),
        )
    }

    single<HomeRepository> {
        HomeRepositoryImpl(
            homeService = get(),
            sleepApiService = get(),
            dispatchersProvider = get(),
            feedingLocalDataSource = get(),
        )
    }

    factory { CalculateBabyAgeUseCase() }
}