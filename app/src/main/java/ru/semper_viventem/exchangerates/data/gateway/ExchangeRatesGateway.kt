package ru.semper_viventem.exchangerates.data.gateway

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.semper_viventem.exchangerates.data.mapToCurrenciesList
import ru.semper_viventem.exchangerates.data.network.Api
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

class ExchangeRatesGateway(
    private val api: Api
) {

    fun getRatesByBaseCurrency(baseCurrency: CurrencyEntity?): Single<List<CurrencyEntity>> {
        return api.latest(baseCurrency?.name)
            .subscribeOn(Schedulers.io())
            .map { it.mapToCurrenciesList(baseCurrency) }
    }
}