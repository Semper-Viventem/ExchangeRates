package ru.semper_viventem.exchangerates.ui

import me.dmdev.rxpm.PresentationModel
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.domain.interactor.GetExchangeRatesInteractor
import ru.semper_viventem.exchangerates.domain.interactor.SetBaseCurrencyInteractor
import ru.semper_viventem.exchangerates.domain.interactor.SetNewFactorInteractor

class MainPm(
    private val getExchangeRatesInteractor: GetExchangeRatesInteractor,
    private val setBaseCurrencyInteractor: SetBaseCurrencyInteractor,
    private val setNewFactorInteractor: SetNewFactorInteractor
) : PresentationModel() {

    val viewState = State<CurrencyRateState>()

    val currencySelected = Action<CurrencyEntity>()
    val factorInput = Action<String>()

    override fun onCreate() {
        super.onCreate()

        getExchangeRatesInteractor.execute()
            .doOnNext(viewState.consumer::accept)
            .retry()
            .subscribe()
            .untilDestroy()

        currencySelected.observable
            .flatMapCompletable(setBaseCurrencyInteractor::execute)
            .subscribe()
            .untilDestroy()

        factorInput.observable
            .map {
                it.toDoubleOrNull() ?: if (it.isBlank()) 0.0 else 1.0
            }
            .flatMapCompletable(setNewFactorInteractor::execute)
            .subscribe()
            .untilDestroy()
    }
}
