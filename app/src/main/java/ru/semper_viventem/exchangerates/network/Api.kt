package ru.semper_viventem.exchangerates.network

import io.reactivex.Completable
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("latest")
    fun latest(@Query("base") base: String): Completable
}