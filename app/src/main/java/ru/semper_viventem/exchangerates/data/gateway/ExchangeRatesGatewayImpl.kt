package ru.semper_viventem.exchangerates.data.gateway

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.semper_viventem.exchangerates.data.network.Api
import ru.semper_viventem.exchangerates.data.toCurrenciesList
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway

class ExchangeRatesGatewayImpl(
    private val api: Api
): ExchangeRatesGateway {

    override fun getRatesByBaseCurrency(baseCurrency: CurrencyEntity?): Single<List<CurrencyEntity>> {
        return api.latest(baseCurrency?.name)
            .subscribeOn(Schedulers.io())
            .map { it.toCurrenciesList(baseCurrency) }
    }
}
