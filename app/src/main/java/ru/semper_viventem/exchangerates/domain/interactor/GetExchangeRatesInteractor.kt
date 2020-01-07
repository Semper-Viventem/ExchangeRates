package ru.semper_viventem.exchangerates.domain.interactor

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables.combineLatest
import io.reactivex.rxkotlin.Observables.zip
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDetailsGateway
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS

class GetExchangeRatesInteractor(
    private val exchangeRatesGateway: ExchangeRatesGateway,
    private val currencyDetailsGateway: CurrencyDetailsGateway,
    private val currencyRateStateGateway: CurrencyRateStateGateway
) {

    fun execute(): Observable<CurrencyRateState> {

        val shouldBeNextTick = BehaviorRelay.createDefault(Unit)
        val tickGenerator = Observable.interval(UPDATE_INTERVAL_MILLIS, MILLISECONDS)

        val multipleTicker = zip(
            shouldBeNextTick.hide().delay(UPDATE_INTERVAL_MILLIS, MILLISECONDS).startWith(Unit),
            tickGenerator.startWith(0)
        ).map { Unit }

        return combineLatest(
            multipleTicker,
            currencyRateStateGateway.getBaseCurrency(),
            currencyRateStateGateway.getFactor()
        )
            .switchMapSingle { (_, baseCurrency, factor) ->
                currencyRateStateGateway.getLastCurrencyRateState()
                    .firstOrError()
                    .map { lastData -> Triple(baseCurrency, factor, lastData) }

            }
            .switchMap { (baseCurrency, factor, lastData) ->
                exchangeRatesGateway.getRatesByBaseCurrency(baseCurrency)
                    .toObservable()
                    .map { buildCurrencyData(it, baseCurrency, factor) }
                    .onErrorReturn { mapCurrencyError(it, lastData, factor) }
                    .flatMap {
                        shouldBeNextTick.accept(Unit)
                        currencyRateStateGateway.setCurrencyRateState(it)
                            .andThen(Observable.just(it))
                    }
                    .withDefault(lastData, factor)
            }

    }

    private fun Observable<CurrencyRateState>.withDefault(
        lastState: CurrencyRateState,
        factor: Double
    ): Observable<CurrencyRateState> {
        return when (lastState) {
            is CurrencyRateState.NoData -> this.startWith(lastState)
            is CurrencyRateState.NotActualCurrencyData -> {
                this.startWith(lastState.copy(lastData = lastState.lastData.actualize(factor)))
            }
            is CurrencyRateState.CurrencyData -> this.startWith(lastState.actualize(factor))
            else -> this
        }
    }

    private fun buildCurrencyData(
        currencies: List<CurrencyEntity>,
        baseCurrency: CurrencyEntity,
        factor: Double
    ): CurrencyRateState {
        return CurrencyRateState.CurrencyData(
            baseCurrency = baseCurrency.fillValues(factor),
            rates = currencies.sortAndFill(factor),
            lastUpdateTime = Date()
        )
    }

    private fun mapCurrencyError(
        exception: Throwable,
        lastState: CurrencyRateState,
        factor: Double
    ): CurrencyRateState {
        return when (lastState) {
            is CurrencyRateState.NoData -> CurrencyRateState.LoadingError(exception)
            is CurrencyRateState.LoadingError -> CurrencyRateState.LoadingError(exception)
            is CurrencyRateState.CurrencyData -> CurrencyRateState.NotActualCurrencyData(
                error = exception,
                lastData = lastState.actualize(factor)
            )
            is CurrencyRateState.NotActualCurrencyData -> lastState.copy(
                error = exception,
                lastData = lastState.lastData.actualize(factor)
            )
        }
    }

    private fun CurrencyRateState.CurrencyData.actualize(factor: Double): CurrencyRateState.CurrencyData {
        return copy(
            baseCurrency = baseCurrency.fillValues(factor),
            rates = rates.sortAndFill(factor)
        )
    }

    private fun List<CurrencyEntity>.sortAndFill(factor: Double = 1.0): List<CurrencyEntity> {
        return map { it.fillValues(factor) }.sortedBy { it.name }
    }

    private fun CurrencyEntity.fillValues(factor: Double = 1.0): CurrencyEntity {
        return copy(
            value = value,
            multipleValue = value * factor,
            fullName = currencyDetailsGateway.getNameForCurrency(this).orEmpty(),
            image = currencyDetailsGateway.getFlagForCurrency(this)
        )
    }

    companion object {
        private const val UPDATE_INTERVAL_MILLIS = 1000L
    }

}
