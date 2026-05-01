package com.diegocunha.thenaapp.feature.feeding.di

import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingViewModel
import com.diegocunha.thenaapp.feature.feeding.repository.FeedingRepositoryImpl
import com.diegocunha.thenaapp.feature.feeding.session.FeedingSessionManager
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val feedingModule = module {

    single<FeedingRepository> {
        FeedingRepositoryImpl(
            sessionDao = get(),
            segmentDao = get(),
        )
    }

    single {
        FeedingSessionManager(
            repository = get(),
            context = androidApplication(),
            dispatchersProvider = get(),
        )
    }

    viewModel {
        FeedingViewModel(sessionManager = get())
    }
}