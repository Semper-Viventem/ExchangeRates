package ru.semper_viventem.exchangerates.data.gateway

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway

class CurrencyRateStateImpl(
    defaultCurrencyEntity: CurrencyEntity,
    defaultFactor: Double
) : CurrencyRateStateGateway {

    private val baseCurrency = BehaviorRelay.createDefault(defaultCurrencyEntity)
    private val factor = BehaviorRelay.createDefault(defaultFactor)
    private val rateState = BehaviorRelay.createDefault<CurrencyRateState>(CurrencyRateState.NoData)

    override fun setBaseCurrency(baseCurrency: CurrencyEntity): Completable {
        return Completable.fromAction { this.baseCurrency.accept(baseCurrency) }
    }

    override fun getBaseCurrency(): Observable<CurrencyEntity> {
        return baseCurrency.hide()
    }

    override fun setFactor(factor: Double): Completable {
        return Completable.fromAction { this.factor.accept(factor) }
    }

    override fun getFactor(): Observable<Double> {
        return factor.hide()
    }

    override fun setCurrencyRateState(currencyRateState: CurrencyRateState): Completable {
        return Completable.fromAction { this.rateState.accept(currencyRateState) }
    }

    override fun getLastCurrencyRateState(): Observable<CurrencyRateState> {
        return rateState.hide()
    }
}
