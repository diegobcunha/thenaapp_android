package com.diegocunha.thenaapp.feature.home.di

import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.presentation.HomeViewModel
import com.diegocunha.thenaapp.feature.home.repository.HomeRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel {
        HomeViewModel(
            homeRepository = get()
        )
    }

    single<HomeRepository> {
        HomeRepositoryImpl(
            userService = get(),
            dispatchersProvider = get(),
            feedingLocalDataSource = get(),
        )
    }
}
