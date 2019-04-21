package ru.semper_viventem.exchangerates.domain

import io.reactivex.Single
import ru.semper_viventem.exchangerates.data.gateway.ExchangeRatesGateway

class GetExchangeRatesInteractor(
    private val exchangeRatesGateway: ExchangeRatesGateway
) {

    fun execute(base: CurrencyEntity): Single<List<CurrencyEntity>> = exchangeRatesGateway.getRatesByBaseCurrency(base)
}