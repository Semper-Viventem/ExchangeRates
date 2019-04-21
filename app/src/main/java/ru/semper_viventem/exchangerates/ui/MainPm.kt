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

    data class CurrencyListItem(
        val currency: CurrencyEntity,
        val isBaseCurrency: Boolean
    )

    val rateAndUpdateTopItem = State(emptyList<CurrencyListItem>() to false)
    val currencySelected = Action<CurrencyEntity>()
    val baseCurrencyInput = Action<String>()
    val changeScrollState = Action<Boolean>()

    private val timer = Observable.interval(UPDATE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)
    private val baseCurrency = State(OptionsProvider.BASE_CURRENCY)
    private val newBaseCurrencyBuffer = State(OptionsProvider.BASE_CURRENCY)
    private val inScrollState = State(false)

    override fun onCreate() {
        super.onCreate()

        timer.withLatestFrom(baseCurrency.observable, newBaseCurrencyBuffer.observable) { _, oldBase, newBase -> oldBase to newBase }
            .filter { !inScrollState.value }
            .flatMapSingle { (oldBase, newBase) ->
                val needToCalculateDiff = !newBase.isSameCurrency(oldBase)

                getExchangeRatesInteractor.execute(newBase)
                    .map { it.map { CurrencyListItem(it, false) } }
                    .map { (listOf(CurrencyListItem(newBase, true)) + it) to needToCalculateDiff }
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

        baseCurrencyInput.observable
            .withLatestFrom(baseCurrency.observable) { newValue, currency ->
                currency.copy(value = newValue.toDoubleOrNull() ?: DEFAULT_VALUE)
            }
            .subscribe(newBaseCurrencyBuffer.consumer)
            .untilDestroy()

        changeScrollState.observable
            .subscribe(inScrollState.consumer)
            .untilDestroy()
    }
}