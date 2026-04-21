package com.diegocunha.thenaapp.core.di

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.coroutines.DispatchersProviderImpl
import org.koin.dsl.module

val coreModule = module {
    single<DispatchersProvider> { DispatchersProviderImpl }
}
