package ru.semper_viventem.exchangerates.domain.interactor

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables.combineLatest
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDetailsGateway
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway
import java.util.*
import java.util.concurrent.TimeUnit

class GetExchangeRatesInteractor(
    private val exchangeRatesGateway: ExchangeRatesGateway,
    private val currencyDetailsGateway: CurrencyDetailsGateway,
    private val currencyRateStateGateway: CurrencyRateStateGateway
) {

    private val updateTimer =
        Observable.interval(UPDATE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)

    fun execute(): Observable<CurrencyRateState> {

        return combineLatest(
            updateTimer.hide(),
            currencyRateStateGateway.getBaseCurrency(),
            currencyRateStateGateway.getFactor()
        ).flatMapSingle { (_, baseCurrency, factor) ->
            currencyRateStateGateway.getLastCurrencyRateState()
                .firstOrError()
                .map { lastData -> Triple(baseCurrency, factor, lastData) }

        }.flatMap { (baseCurrency, factor, lastData) ->
            exchangeRatesGateway.getRatesByBaseCurrency(baseCurrency)
                .toObservable()
                .run {
                    when (lastData) {
                        is CurrencyRateState.CurrencyData -> this.defaultIfEmpty(lastData.rates)
                        is CurrencyRateState.NotActualCurrencyData -> this.defaultIfEmpty(lastData.lastData.rates)
                        else -> this
                    }
                }
                .map {
                    mapCurrency(
                        it.map { it.fillValues(factor) },
                        baseCurrency.fillValues()
                    )
                }
                .onErrorReturn { mapCurrencyError(it, lastData, factor) }
                .flatMap {
                    currencyRateStateGateway.setCurrencyRateState(it)
                        .andThen(Observable.just(it))
                }
                .run {
                    if (lastData is CurrencyRateState.NoData) {
                        this.startWith(lastData)
                    } else {
                        this
                    }
                }
        }

    }

    private fun mapCurrency(
        currencies: List<CurrencyEntity>,
        baseCurrency: CurrencyEntity
    ): CurrencyRateState {
        return CurrencyRateState.CurrencyData(
            baseCurrency = baseCurrency,
            rates = currencies,
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
                exception,
                lastState.copy(
                    rates = lastState.rates.map { it.fillValues(factor) }
                )
            )
            is CurrencyRateState.NotActualCurrencyData -> lastState.copy(
                error = exception,
                lastData = lastState.lastData.copy(
                    rates = lastState.lastData.rates.map { it.fillValues(factor) }
                )
            )
        }
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
        private const val UPDATE_INTERVAL_MILLISECONDS = 1000L
    }

}
