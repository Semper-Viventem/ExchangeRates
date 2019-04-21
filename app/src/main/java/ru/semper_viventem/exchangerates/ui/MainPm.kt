package ru.semper_viventem.exchangerates.ui

import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import me.dmdev.rxpm.PresentationModel
import ru.semper_viventem.exchangerates.OptionsProvider
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.GetExchangeRatesInteractor
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainPm(
    private val getExchangeRatesInteractor: GetExchangeRatesInteractor
) : PresentationModel() {

    companion object {
        private const val UPDATE_INTERVAL_MILLISECONDS = 1000L
        private const val DEFAULT_VALUE = 1.0
    }

    val rateAndUpdateTopItem = State(emptyList<CurrencyEntity>() to false)
    val currencySelected = Action<CurrencyEntity>()

    private val timer = Observable.interval(UPDATE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)
    private val baseCurrency = State(OptionsProvider.BASE_CURRENCY)
    private val newBaseCurrencyBuffer = State(OptionsProvider.BASE_CURRENCY)

    override fun onCreate() {
        super.onCreate()

        timer.withLatestFrom(baseCurrency.observable, newBaseCurrencyBuffer.observable) { _, oldBase, newBase -> oldBase to newBase }
            .flatMapSingle { (oldBase, newBase) ->
                val needToCalculateDiff = !newBase.isSameCurrency(oldBase)

                getExchangeRatesInteractor.execute(newBase)
                    .map { (listOf(newBase) + it) to needToCalculateDiff }
                    .doOnSuccess {
                        if (needToCalculateDiff) {
                            baseCurrency.consumer.accept(newBase)
                        }
                    }
            }
            .doOnNext(rateAndUpdateTopItem.consumer)
            .doOnError { error -> Timber.e(error) }
            .retry()
            .subscribe()
            .untilDestroy()

        currencySelected.observable
            .map { it.copy(value = DEFAULT_VALUE) }
            .subscribe(newBaseCurrencyBuffer.consumer)
            .untilDestroy()
    }
}