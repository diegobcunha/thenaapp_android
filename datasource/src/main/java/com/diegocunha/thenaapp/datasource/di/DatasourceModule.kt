package com.diegocunha.thenaapp.datasource.di

import androidx.credentials.CredentialManager
import com.diegocunha.thenaapp.datasource.BuildConfig
import com.diegocunha.thenaapp.datasource.network.ThenaAppService
import com.diegocunha.thenaapp.datasource.network.createRetrofit
import com.diegocunha.thenaapp.datasource.network.interceptor.AccessTokenRepository
import com.diegocunha.thenaapp.datasource.network.interceptor.AccessTokenRepositoryImpl
import com.diegocunha.thenaapp.datasource.network.interceptor.HeaderInterceptor
import com.diegocunha.thenaapp.datasource.network.service.BabyService
import com.diegocunha.thenaapp.datasource.network.service.CloudinaryService
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.datasource.repository.LoginCredentialsManager
import com.diegocunha.thenaapp.datasource.repository.UserSessionRepository
import com.diegocunha.thenaapp.datasource.repository.UserSessionRepositoryImpl
import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferencesImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val CLOUDINARY_NAME = "cloudinary"
private const val CLOUDINARY_URL = "https://api.cloudinary.com/"
private const val APPLICATION_JSON_KEY = "application/json"

val datasourceModule = module {

    factory<FirebaseAuth> { FirebaseAuth.getInstance() }

    factory<OkHttpClient.Builder> {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    level =
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                )
            )
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
    single {
        createRetrofit(
            okHttpClient = get<OkHttpClient.Builder>()
                .addInterceptor(get<HeaderInterceptor>()).build(),
            json = get()
        )
    }
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

    single(named(CLOUDINARY_NAME)) {
        Retrofit.Builder()
            .baseUrl(CLOUDINARY_URL)
            .client(get<OkHttpClient.Builder>().build())
            .addConverterFactory(get<Json>().asConverterFactory(APPLICATION_JSON_KEY.toMediaType()))
            .build()
    }

    single<CloudinaryService> { get<Retrofit>(named(CLOUDINARY_NAME)).create(CloudinaryService::class.java) }
}
