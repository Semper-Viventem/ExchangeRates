package ru.semper_viventem.exchangerates.data.network.response

data class ExchangeRatesResponse(
    val base: String,
    val date: String,
    val rates: ExchangeRatesResponse
)