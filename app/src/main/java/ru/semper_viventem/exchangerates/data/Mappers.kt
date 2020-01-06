package ru.semper_viventem.exchangerates.data

import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

fun ExchangeRatesResponse.toCurrenciesList(): List<CurrencyEntity> {
    return this.ratesResponse.rates.map { (currencyName, currencyValue) ->
        CurrencyEntity(
            name = currencyName,
            value = currencyValue
        )
    }
}
