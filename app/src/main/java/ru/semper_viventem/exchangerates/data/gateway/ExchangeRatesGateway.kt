package ru.semper_viventem.exchangerates.data.gateway

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.semper_viventem.exchangerates.data.network.Api
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

class ExchangeRatesGateway(
    private val api: Api
) {

    fun getRatesByBaseCurrency(baseCurrency: CurrencyEntity?): Single<List<CurrencyEntity>> {
        return api.latest(baseCurrency?.name)
            .subscribeOn(Schedulers.io())
            .map { extractCurrencies(it, baseCurrency) }
    }

    private fun extractCurrencies(exchangeRatesResponse: ExchangeRatesResponse, base: CurrencyEntity?): List<CurrencyEntity> {
        val baseCurrency = base
            ?: CurrencyEntity(name = exchangeRatesResponse.base, isBase = true)
        val rates = exchangeRatesResponse.ratesResponse.rates.map { (currencyName, currencyValue) ->
            CurrencyEntity(
                name = currencyName,
                value = currencyValue
            )
        }
        return listOf(baseCurrency) + rates
    }
}