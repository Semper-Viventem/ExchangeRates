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

    val rateAndAnimateTopItem = State(emptyList<CurrencyEntity>() to false)
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
            .doOnNext(rateAndAnimateTopItem.consumer)
            .doOnError { error -> Timber.e(error) }
            .retry()
            .subscribe()
            .untilDestroy()

        currencySelected.observable
            .filter(::isDifferentBaseElement)
            .map(::mapToBaseElement)
            .doOnNext(baseCurrency.consumer)
            .withLatestFrom(
                rateAndAnimateTopItem.observable.map { it.first },
                ::getUpdatedCurrencyList
            )
            .subscribe(rateAndAnimateTopItem.consumer)
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

    private fun isDifferentBaseElement(newBase: CurrencyEntity): Boolean {
        return !(baseCurrency.valueOrNull != null && baseCurrency.value.isSameCurrency(newBase))
    }

    private fun mapToBaseElement(element: CurrencyEntity): CurrencyEntity {
        return element.copy(value = CurrencyEntity.DEFAULT_CURRENCY_VALUE, isBase = true)
    }

    private fun getUpdatedCurrencyList(newBase: CurrencyEntity, allCurrency: List<CurrencyEntity>): Pair<List<CurrencyEntity>, Boolean> {
        val result = mutableListOf<CurrencyEntity>()

        result.add(newBase)

        allCurrency
            .sortedBy { it.name }
            .forEach {
                if (!it.isSameCurrency(newBase)) {
                    result.add(it.copy(isBase = false))
                }
            }

        return result to true
    }
}