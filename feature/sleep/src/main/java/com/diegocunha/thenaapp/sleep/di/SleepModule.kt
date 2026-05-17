package com.diegocunha.thenaapp.sleep.di

import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.presentation.SleepStatisticsViewModel
import com.diegocunha.thenaapp.sleep.presentation.SleepViewModel
import com.diegocunha.thenaapp.sleep.repository.SleepRepositoryImpl
import com.diegocunha.thenaapp.sleep.session.SleepSessionManager
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sleepModule = module {

    single<SleepRepository> {
        SleepRepositoryImpl(
            sleepApiService = get(),
            dispatchersProvider = get(),
        )
    }

    single {
        SleepSessionManager(
            repository = get(),
            context = androidApplication(),
            dispatchersProvider = get(),
        )
    }

    viewModel { (babyId: String) ->
        SleepViewModel(sessionManager = get(), repository = get(), babyId = babyId)
    }

    viewModel { (babyId: String) ->
        SleepStatisticsViewModel(repository = get(), babyId = babyId)
    }
}
