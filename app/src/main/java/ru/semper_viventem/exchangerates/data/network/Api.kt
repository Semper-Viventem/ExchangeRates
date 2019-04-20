package ru.semper_viventem.exchangerates.data.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse

interface Api {

    @GET("latest")
    fun latest(@Query("base") base: String): Single<ExchangeRatesResponse>
}