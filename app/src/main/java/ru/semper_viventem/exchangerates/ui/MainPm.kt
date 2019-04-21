package ru.semper_viventem.exchangerates.ui

import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import me.dmdev.rxpm.PresentationModel
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.GetExchangeRatesInteractor
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainPm(
    private val getExchangeRatesInteractor: GetExchangeRatesInteractor
) : PresentationModel() {

    companion object {
        private const val UPDATE_INTERVAL_MILLISECONDS = 1000L
    }

    private val defaultCurrency = CurrencyEntity(
        name = "EUR",
        fullName = "Euro",
        value = 1.0,
        imageRes = null
    )

    val rateAndUpdateTopItem = State(emptyList<CurrencyEntity>() to false)
    val currencySelected = Action<CurrencyEntity>()

    private val timer = Observable.interval(UPDATE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)
    private val baseCurrency = State(defaultCurrency)
    private val newBaseCurrencyBuffer = State(defaultCurrency)

    override fun onCreate() {
        super.onCreate()

        timer.withLatestFrom(baseCurrency.observable, newBaseCurrencyBuffer.observable) { _, oldBase, newBase -> oldBase to newBase }
            .flatMapSingle { (oldBase, newBase) ->
                getExchangeRatesInteractor.execute(newBase)
                    .map {
                        val needToCalculateDiff = newBase.name != oldBase.name
                        val rateList = (listOf(newBase) + it)
                        rateList to needToCalculateDiff
                    }
                    .doOnSuccess { baseCurrency.consumer.accept(newBase) }
            }
            .doOnNext(rateAndUpdateTopItem.consumer)
            .doOnError { error -> Timber.e(error) }
            .retry()
            .subscribe()
            .untilDestroy()

        currencySelected.observable
            .subscribe(newBaseCurrencyBuffer.consumer)
            .untilDestroy()
    }
}