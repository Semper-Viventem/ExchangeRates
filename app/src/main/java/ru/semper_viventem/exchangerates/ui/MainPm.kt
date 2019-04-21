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
                    .map { mapCurrencyEntities(it) }
                    .map { it to false }
            }
            .doOnNext(rateAndUpdateTopItem.consumer)
            .doOnError { error -> Timber.e(error) }
            .retry()
            .subscribe()
            .untilDestroy()

        currencySelected.observable
            .map { it.copy(value = DEFAULT_VALUE) }
            .withLatestFrom(baseCurrency.observable, rateAndUpdateTopItem.observable) { newBase, oldBase, rate ->
                Triple(newBase, oldBase, rate)
            }
            .doOnNext { (newBase, _, _) ->
                baseCurrency.consumer.accept(newBase)
            }
            .filter { (newBase, oldBase, _) ->
                !newBase.isSameCurrency(oldBase)
            }
            .map { (newBase, _, rate) ->
                val currencyItems = getCurrencyListWithoutBase(rate.first, newBase)
                val firstCurrencyItem = CurrencyListItem(newBase, true)
                (listOf(firstCurrencyItem) + currencyItems) to true
            }
            .subscribe(rateAndUpdateTopItem.consumer)
            .untilDestroy()

        baseCurrencyInput.observable
            .withLatestFrom(baseCurrency.observable) { newValue, currency ->
                currency.copy(value = newValue.toDoubleOrNull() ?: DEFAULT_VALUE)
            }
            .subscribe(baseCurrency.consumer)
            .untilDestroy()

        changeScrollState.observable
            .subscribe(inScrollState.consumer)
            .untilDestroy()
    }

    private fun mapCurrencyEntities(currencies: List<CurrencyEntity>): List<CurrencyListItem> {
        return currencies.mapIndexed { i, currency ->
            getCurrencyListItem(currency, i)
        }
    }

    private fun getCurrencyListItem(currency: CurrencyEntity, position: Int): CurrencyListItem {
        return CurrencyListItem(currency, position == 0)
    }

    private fun getCurrencyListWithoutBase(allCurrency: List<CurrencyListItem>, baseCurrency: CurrencyEntity): List<CurrencyListItem> {
        return allCurrency
            .map { it.copy(isBaseCurrency = false) }
            .filter { !it.currency.isSameCurrency(baseCurrency) }
            .sortedBy { it.currency.name }
    }
}