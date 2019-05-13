package ru.semper_viventem.exchangerates.domain.gateway

import io.reactivex.Single
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

interface ExchangeRatesGateway {

    fun getRatesByBaseCurrency(baseCurrency: CurrencyEntity?): Single<List<CurrencyEntity>>
}