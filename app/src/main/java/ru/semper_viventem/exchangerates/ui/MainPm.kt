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

    val rate = State<List<CurrencyEntity>>()
    val currencySelected = Action<CurrencyEntity>()

    private val baseCurrency = State(
        CurrencyEntity(
            name = "EUR",
            fullName = "Euro",
            value = 1.0,
            imageRes = null
        )
    )

    val timer = Observable.interval(UPDATE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)

    override fun onCreate() {
        super.onCreate()

        currencySelected.observable
            .subscribe(baseCurrency.consumer)
            .untilDestroy()

        timer.withLatestFrom(baseCurrency.observable) { _, base -> base }
            .flatMapSingle { baseCurrency ->
                getExchangeRatesInteractor.execute(baseCurrency)
                    .map { listOf(baseCurrency) + it }
            }
            .doOnNext(rate.consumer)
            .doOnError { error -> Timber.e(error) }
            .retry()
            .subscribe()
            .untilDestroy()
    }
}