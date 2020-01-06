package ru.semper_viventem.exchangerates.domain.interactor

import io.reactivex.Completable
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway

class SetBaseCurrencyInteractor(
    private val currencyRateStateGateway: CurrencyRateStateGateway
) {

    fun execute(baseCurrency: CurrencyEntity): Completable = currencyRateStateGateway.setBaseCurrency(baseCurrency)
}
