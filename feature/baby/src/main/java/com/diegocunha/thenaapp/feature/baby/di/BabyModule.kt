package com.diegocunha.thenaapp.feature.baby.di

import com.diegocunha.thenaapp.feature.baby.domain.create.CreateBabyRepository
import com.diegocunha.thenaapp.feature.baby.presentation.create.CreateBabyViewModel
import com.diegocunha.thenaapp.feature.baby.repository.create.CreateBabyRepositoryImpl
import com.diegocunha.thenaapp.feature.baby.repository.create.ImageCompressor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val babyModule = module {

    viewModel {
        CreateBabyViewModel(createBabyRepository = get())
    }

    single {
        ImageCompressor(
            context = androidApplication(),
            dispatchersProvider = get(),
        )
    }

    single<CreateBabyRepository> {
        CreateBabyRepositoryImpl(
            service = get(),
            cloudinaryService = get(),
            dispatchersProvider = get(),
            imageCompressor = get(),
        )
    }
}
