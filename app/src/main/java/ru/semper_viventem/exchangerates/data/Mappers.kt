package ru.semper_viventem.exchangerates.data

import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

fun ExchangeRatesResponse.mapToCurrenciesList(base: CurrencyEntity?): List<CurrencyEntity> {

    val baseCurrency = base
        ?: CurrencyEntity(name = this.base, isBase = true)
    val rates = this.ratesResponse.rates.map { (currencyName, currencyValue) ->
        CurrencyEntity(
            name = currencyName,
            value = currencyValue
        )
    }
    return listOf(baseCurrency) + rates
}