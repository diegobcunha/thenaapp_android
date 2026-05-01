package com.diegocunha.thenaapp

import android.app.Application
import com.diegocunha.thenaapp.core.di.coreModule
import com.diegocunha.thenaapp.datasource.di.datasourceModule
import com.diegocunha.thenaapp.di.appModule
import com.diegocunha.thenaapp.feature.baby.di.babyModule
import com.diegocunha.thenaapp.feature.feeding.di.feedingModule
import com.diegocunha.thenaapp.feature.home.di.homeModule
import com.diegocunha.thenaapp.feature.login.di.loginModule
import com.diegocunha.thenaapp.feature.onboarding.di.onboardingModule
import com.diegocunha.thenaapp.feature.signup.di.signupModule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class ThenaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            ComposeStabilityAnalyzer.setEnabled(true)
        }

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@ThenaApplication)
            modules(
                appModule,
                coreModule,
                datasourceModule,
                loginModule,
                signupModule,
                onboardingModule,
                babyModule,
                homeModule,
                feedingModule,
            )
        }
    }
}
