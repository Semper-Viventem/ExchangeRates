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
            .map { extractCurrencies(it) }
    }

    private fun extractCurrencies(exchangeRatesResponse: ExchangeRatesResponse): List<CurrencyEntity> {
        val baseCurrency = CurrencyEntity(exchangeRatesResponse.base)
        val rates = exchangeRatesResponse.ratesResponse.rates.map { (currencyName, currencyValue) ->
            CurrencyEntity(
                name = currencyName,
                fullName = "",
                value = currencyValue,
                image = null
            )
        }
        return listOf(baseCurrency) + rates
    }
}