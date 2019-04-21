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

    val rateAndUpdateTopItem = State(emptyList<CurrencyEntity>() to false)
    val currencySelected = Action<CurrencyEntity>()
    val baseCurrencyInput = Action<String>()
    val changeScrollState = Action<Boolean>()

    private val updateTimer = Observable.interval(UPDATE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)
    private val baseCurrency = State<CurrencyEntity>()
    private val inScrollState = State(false)

    override fun onCreate() {
        super.onCreate()

        updateTimer
            .filter { !inScrollState.value }
            .flatMapSingle {
                getExchangeRatesInteractor.execute(baseCurrency.valueOrNull)
                    .doOnSuccess {
                        baseCurrency.consumer.accept(it.first())
                    }
                    .map { it to false }
            }
            .doOnNext(rateAndUpdateTopItem.consumer)
            .doOnError { error -> Timber.e(error) }
            .retry()
            .subscribe()
            .untilDestroy()

        currencySelected.observable
            .filter { newBase ->
                baseCurrency.valueOrNull?.isSameCurrency(newBase) == false
            }
            .map { it.copy(value = CurrencyEntity.DEFAULT_CURRENCY_VALUE, isBase = true) }
            .withLatestFrom(baseCurrency.observable, rateAndUpdateTopItem.observable) { newBase, oldBase, rate ->
                Triple(newBase, oldBase, rate)
            }
            .doOnNext { (newBase, _, _) ->
                baseCurrency.consumer.accept(newBase)
            }
            .map { (newBase, _, rate) ->
                val currencyItems = getCurrencyListWithoutBase(rate.first, newBase)
                (listOf(newBase) + currencyItems) to true
            }
            .subscribe(rateAndUpdateTopItem.consumer)
            .untilDestroy()

        baseCurrencyInput.observable
            .withLatestFrom(baseCurrency.observable) { newValue, currency ->
                currency.copy(
                    value = newValue.toDoubleOrNull() ?: CurrencyEntity.DEFAULT_CURRENCY_VALUE
                )
            }
            .subscribe(baseCurrency.consumer)
            .untilDestroy()

        changeScrollState.observable
            .subscribe(inScrollState.consumer)
            .untilDestroy()
    }

    private fun getCurrencyListWithoutBase(allCurrency: List<CurrencyEntity>, baseCurrency: CurrencyEntity): List<CurrencyEntity> {
        return allCurrency
            .map { it.copy(isBase = false) }
            .filter { !it.isSameCurrency(baseCurrency) }
            .sortedBy { it.name }
    }
}