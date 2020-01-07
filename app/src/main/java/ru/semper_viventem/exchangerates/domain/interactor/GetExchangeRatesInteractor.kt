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
            .startWith(0)

    fun execute(): Observable<CurrencyRateState> {
        return combineLatest(
            updateTimer.hide(),
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
                    .map {
                        mapCurrency(
                            it.sortAndFill(factor),
                            baseCurrency.fillValues()
                        )
                    }
                    .onErrorReturn { mapCurrencyError(it, lastData, factor) }
                    .flatMap {
                        currencyRateStateGateway.setCurrencyRateState(it)
                            .andThen(Observable.just(it))
                    }
                    .run {
                        when (lastData) {
                            is CurrencyRateState.NoData -> this.startWith(lastData)
                            is CurrencyRateState.NotActualCurrencyData -> {
                                this.startWith(
                                    lastData.copy(
                                        lastData = lastData.lastData.copy(
                                            rates = lastData.lastData.rates.sortAndFill(
                                                factor
                                            )
                                        )
                                    )
                                )
                            }
                            is CurrencyRateState.CurrencyData -> {
                                this.startWith(
                                    lastData.copy(
                                        rates = lastData.rates.sortAndFill(
                                            factor
                                        )
                                    )
                                )
                            }
                            else -> this
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
                    rates = lastState.rates.sortAndFill(factor)
                )
            )
            is CurrencyRateState.NotActualCurrencyData -> lastState.copy(
                error = exception,
                lastData = lastState.lastData.copy(
                    rates = lastState.lastData.rates.sortAndFill(factor)
                )
            )
        }
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
        private const val UPDATE_INTERVAL_MILLISECONDS = 2000L
    }

}