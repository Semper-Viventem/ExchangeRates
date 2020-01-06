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
                .map { mapCurrency(it, baseCurrency, factor) }
                .onErrorReturn { mapCurrencyError(it, lastData) }
                .flatMap {
                    currencyRateStateGateway.setCurrencyRateState(it)
                        .andThen(Observable.just(it))
                }
        }

    }

    private fun mapCurrency(
        currencies: List<CurrencyEntity>,
        baseCurrency: CurrencyEntity,
        factor: Double
    ): CurrencyRateState {

        val updatedRates = currencies.map {
            it.copy(
                value = it.value * factor,
                fullName = currencyDetailsGateway.getNameForCurrency(it).orEmpty(),
                image = currencyDetailsGateway.getFlagForCurrency(it)
            )
        }

        val data = CurrencyRateState.CurrencyData(
            baseCurrency = baseCurrency,
            rates = updatedRates,
            factor = factor,
            lastUpdateTime = Date()
        )

        return data
    }

    private fun mapCurrencyError(
        exception: Throwable,
        lastData: CurrencyRateState
    ): CurrencyRateState {
        return when (lastData) {
            is CurrencyRateState.NoData -> CurrencyRateState.LoadingError(exception)
            is CurrencyRateState.LoadingError -> CurrencyRateState.LoadingError(exception)
            is CurrencyRateState.CurrencyData -> CurrencyRateState.NotActualCurrencyData(
                exception,
                lastData
            )
            is CurrencyRateState.NotActualCurrencyData -> lastData.copy(error = exception)
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_MILLISECONDS = 1000L
    }

}
