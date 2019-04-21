package ru.semper_viventem.exchangerates.data.gateway

import org.koin.dsl.module

object GatewayModule {
    val module = module {
        single { ExchangeRatesGateway(get()) }
        single { CurrencyDataGateway(get()) }
    }
}