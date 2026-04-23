package com.diegocunha.thenaapp.feature.login.di

import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.diegocunha.thenaapp.feature.login.presentation.LoginViewModel
import com.diegocunha.thenaapp.feature.login.repository.LoginRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {

    viewModel {
        LoginViewModel(
            loginRepository = get(),
        )
    }

    single<LoginRepository> {
        LoginRepositoryImpl(
            userService = get(),
            dispatchersProvider = get(),
            firebaseAuth = get(),
            loginCredentialsManager = get()
        )
    }
}
