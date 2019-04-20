package ru.semper_viventem.exchangerates.data.gateway

import io.reactivex.Single
import ru.semper_viventem.exchangerates.data.network.Api
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

class ExchangeRatesGateway(
    private val api: Api
) {

    fun getRatesByBaseCurrency(baseCurrency: CurrencyEntity): Single<List<CurrencyEntity>> {
        return api.latest(baseCurrency.name)
            .map { extractCurrencies(it) }
    }

    private fun extractCurrencies(exchangeRatesResponse: ExchangeRatesResponse): List<CurrencyEntity> {
        return emptyList()
    }
}