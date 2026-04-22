package com.diegocunha.thenaapp

import android.app.Application
import com.diegocunha.thenaapp.core.di.coreModule
import com.diegocunha.thenaapp.datasource.di.datasourceModule
import com.diegocunha.thenaapp.feature.login.di.loginModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class ThenaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@ThenaApplication)
            modules(
                coreModule,
                datasourceModule,
                loginModule,
            )
        }
    }
}