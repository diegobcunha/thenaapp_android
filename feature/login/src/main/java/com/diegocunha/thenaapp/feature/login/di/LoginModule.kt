package com.diegocunha.thenaapp.feature.login.di

import androidx.credentials.CredentialManager
import com.diegocunha.thenaapp.feature.login.domain.LoginCredentialsManager
import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.diegocunha.thenaapp.feature.login.presentation.LoginViewModel
import com.diegocunha.thenaapp.feature.login.repository.LoginRepositoryImpl
import org.koin.android.ext.koin.androidApplication
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

    single<CredentialManager> {
        CredentialManager.create(androidApplication())
    }

    single {
        LoginCredentialsManager(
            credentialManager = get(),
            context = androidApplication()
        )
    }
}