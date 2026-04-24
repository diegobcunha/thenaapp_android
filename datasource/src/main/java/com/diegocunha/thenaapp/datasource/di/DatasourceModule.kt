package com.diegocunha.thenaapp.datasource.di

import androidx.credentials.CredentialManager
import com.diegocunha.thenaapp.datasource.BuildConfig
import com.diegocunha.thenaapp.datasource.network.ThenaAppService
import com.diegocunha.thenaapp.datasource.network.createRetrofit
import com.diegocunha.thenaapp.datasource.network.interceptor.AccessTokenRepository
import com.diegocunha.thenaapp.datasource.network.interceptor.AccessTokenRepositoryImpl
import com.diegocunha.thenaapp.datasource.network.interceptor.HeaderInterceptor
import com.diegocunha.thenaapp.datasource.network.service.BabyService
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.datasource.repository.LoginCredentialsManager
import com.diegocunha.thenaapp.datasource.repository.UserSessionRepository
import com.diegocunha.thenaapp.datasource.repository.UserSessionRepositoryImpl
import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferencesImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit

val datasourceModule = module {

    factory<FirebaseAuth> { FirebaseAuth.getInstance() }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get<HeaderInterceptor>())
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    level =
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                )
            )
            .build()
    }

    single {
        HeaderInterceptor(
            accessTokenRepository = get()
        )
    }

    single<AccessTokenRepository> {
        AccessTokenRepositoryImpl(
            firebaseAuth = get(),
        )
    }

    single { Json { ignoreUnknownKeys = true } }
    single { createRetrofit(okHttpClient = get(), json = get()) }
    single { get<Retrofit>().create(ThenaAppService::class.java) }
    single { get<Retrofit>().create(UserService::class.java) }
    single { get<Retrofit>().create(BabyService::class.java) }

    single<CredentialManager> {
        CredentialManager.create(androidApplication())
    }

    single {
        LoginCredentialsManager(
            credentialManager = get(),
            context = androidApplication()
        )
    }

    single<CustomSharedPreferences> {
        CustomSharedPreferencesImpl(
            context = androidApplication(),
            dispatchersProvider = get()
        )
    }

    single<UserSessionRepository> {
        UserSessionRepositoryImpl(preferences = get())
    }
}
