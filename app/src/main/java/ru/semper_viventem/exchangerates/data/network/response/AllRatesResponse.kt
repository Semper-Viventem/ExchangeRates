package ru.semper_viventem.exchangerates.data.network.response

data class AllRatesResponse(
    val rates: List<Pair<String, Double>>
)