package ru.semper_viventem.exchangerates.domain.interactor

import io.reactivex.Completable
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway

class SetNewFactorInteractor(
    private val currencyRateStateGateway: CurrencyRateStateGateway
) {

    fun execute(factor: Double): Completable = currencyRateStateGateway.setFactor(factor)
}
