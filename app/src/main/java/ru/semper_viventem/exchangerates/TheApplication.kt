package ru.semper_viventem.exchangerates

import android.app.Application
import com.facebook.stetho.Stetho
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.semper_viventem.exchangerates.data.gateway.GatewayModule
import ru.semper_viventem.exchangerates.data.network.NetworkModule
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.interactor.InteractorModule
import ru.semper_viventem.exchangerates.ui.UIModule
import timber.log.Timber

class TheApplication : Application() {

    companion object {
        private const val DEFAULT_CURRENCY_FACTOR = 1.0
        private val DEFAULT_BASE_CURRENCY = CurrencyEntity(
            name = "EUR",
            value = DEFAULT_CURRENCY_FACTOR
        )
    }

    override fun onCreate() {
        super.onCreate()

        initDI()
        initLog()
        initStetho()
    }

    private fun initDI() {
        startKoin {
            androidContext(this@TheApplication)
            androidLogger()
            modules(
                UIModule.module,
                NetworkModule.module,
                GatewayModule.module(DEFAULT_BASE_CURRENCY, DEFAULT_CURRENCY_FACTOR),
                InteractorModule.module(DEFAULT_CURRENCY_FACTOR)
            )
        }
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }
}
