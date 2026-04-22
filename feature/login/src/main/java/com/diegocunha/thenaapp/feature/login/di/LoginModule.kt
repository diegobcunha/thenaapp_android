package com.diegocunha.thenaapp.feature.login.di

import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.diegocunha.thenaapp.feature.login.presentation.LoginViewModel
import com.diegocunha.thenaapp.feature.login.repository.LoginRepositoryImpl
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {

    viewModel { LoginViewModel(get()) }

    single<LoginRepository> {
        LoginRepositoryImpl(
            userService = get(),
            dispatchersProvider = get(),
            firebaseAuth = get(),
        )
    }

    single {
        val context = androidContext()
        val clientIdResId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(clientIdResId))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
}