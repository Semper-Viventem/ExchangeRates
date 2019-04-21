package ru.semper_viventem.exchangerates.ui

import io.reactivex.functions.Consumer
import me.dmdev.rxpm.PresentationModel
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.GetExchangeRatesInteractor
import timber.log.Timber

class MainPm(
    private val getExchangeRatesInteractor: GetExchangeRatesInteractor
) : PresentationModel() {

    val rate = State<List<CurrencyEntity>>()

    override fun onCreate() {
        super.onCreate()

        val baseCurrency = CurrencyEntity(
            name = "EUR",
            fullName = "Euro",
            value = 1.0,
            imageRes = null
        )

        getExchangeRatesInteractor.execute(baseCurrency)
            .subscribe(rate.consumer, Consumer { error -> Timber.e(error) })
            .untilDestroy()
    }
}