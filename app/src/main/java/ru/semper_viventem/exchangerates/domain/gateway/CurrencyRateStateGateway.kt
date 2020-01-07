package ru.semper_viventem.exchangerates.domain.gateway

import io.reactivex.Completable
import io.reactivex.Observable
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState

interface CurrencyRateStateGateway {

    fun setBaseCurrency(baseCurrency: CurrencyEntity): Completable

    fun getBaseCurrency(): Observable<CurrencyEntity>

    fun setCurrencyRateState(currencyRateState: CurrencyRateState): Completable

    fun getLastCurrencyRateState(): Observable<CurrencyRateState>

    fun setFactor(factor: Double): Completable

    fun getFactor(): Observable<Double>
}
