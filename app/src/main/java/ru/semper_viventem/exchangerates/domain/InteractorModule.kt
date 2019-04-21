package ru.semper_viventem.exchangerates.domain

import org.koin.dsl.module

object InteractorModule {
    val module = module {
        factory { GetExchangeRatesInteractor(get()) }
    }
}