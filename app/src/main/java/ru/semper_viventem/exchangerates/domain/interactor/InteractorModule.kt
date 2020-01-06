package ru.semper_viventem.exchangerates.domain.interactor

import org.koin.dsl.module

object InteractorModule {
    val module = module {
        factory { GetExchangeRatesInteractor(get(), get(), get()) }
        factory { SetBaseCurrencyInteractor(get()) }
        factory { SetNewFactorInteractor(get()) }
    }
}
