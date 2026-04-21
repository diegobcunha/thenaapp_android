package com.diegocunha.thenaapp.datasource.di

import com.diegocunha.thenaapp.datasource.BuildConfig
import com.diegocunha.thenaapp.datasource.network.ThenaAppService
import com.diegocunha.thenaapp.datasource.network.createOkHttpClient
import com.diegocunha.thenaapp.datasource.network.createRetrofit
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import retrofit2.Retrofit

val datasourceModule = module {
    single { createOkHttpClient(isDebug = BuildConfig.DEBUG) }
    single { Json { ignoreUnknownKeys = true } }
    single { createRetrofit(okHttpClient = get(), json = get()) }
    single { get<Retrofit>().create(ThenaAppService::class.java) }
}
