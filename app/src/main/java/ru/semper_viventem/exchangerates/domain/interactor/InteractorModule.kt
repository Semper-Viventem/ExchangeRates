package ru.semper_viventem.exchangerates.domain.interactor

import org.koin.dsl.module

object InteractorModule {
    fun module(defaultFactor: Double) = module {
        factory { GetExchangeRatesInteractor(get(), get(), get()) }
        factory { SetBaseCurrencyInteractor(get(), defaultFactor) }
        factory { SetNewFactorInteractor(get()) }
    }
}
