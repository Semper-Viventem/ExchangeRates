package ru.semper_viventem.exchangerates.data.network.response

import com.google.gson.annotations.SerializedName

data class ExchangeRatesResponse(
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val ratesResponse: AllRatesResponse
)