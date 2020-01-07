package ru.semper_viventem.exchangerates.data.gateway

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDetailsGateway
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway

object GatewayModule {

    fun module(
        defaultBaseCurrency: CurrencyEntity,
        defaultFactor: Double
    ) = module {
        single { ExchangeRatesGatewayImpl(get()) } bind ExchangeRatesGateway::class
        single { CurrencyDetailsGatewayImpl(get()) } bind CurrencyDetailsGateway::class
        single { CurrencyRateStateImpl(defaultBaseCurrency, defaultFactor) } bind CurrencyRateStateGateway::class
    }
}
