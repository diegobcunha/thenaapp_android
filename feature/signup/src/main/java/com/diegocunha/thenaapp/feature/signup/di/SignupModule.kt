package com.diegocunha.thenaapp.feature.signup.di

import com.diegocunha.thenaapp.feature.signup.domain.SignupRepository
import com.diegocunha.thenaapp.feature.signup.presentation.SignupViewModel
import com.diegocunha.thenaapp.feature.signup.repository.SignupRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val signupModule = module {

    single<SignupRepository> {
        SignupRepositoryImpl(
            userService = get(),
            firebaseAuth = get(),
            dispatchersProvider = get(),
            loginCredentialsManager = get(),
        )
    }

    viewModel {
        SignupViewModel(
            signupRepository = get(),
        )
    }
}
