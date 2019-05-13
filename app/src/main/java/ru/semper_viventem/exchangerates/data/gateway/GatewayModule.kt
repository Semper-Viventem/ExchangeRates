package ru.semper_viventem.exchangerates.data.gateway

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDataGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway

object GatewayModule {
    val module = module {
        single { ExchangeRatesGatewayImpl(get()) } bind ExchangeRatesGateway::class
        single { CurrencyDataGatewayImpl(get()) } bind CurrencyDataGateway::class
    }
}