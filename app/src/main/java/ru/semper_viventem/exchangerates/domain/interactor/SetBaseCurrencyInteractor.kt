package ru.semper_viventem.exchangerates.domain.interactor

import io.reactivex.Completable
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway

class SetBaseCurrencyInteractor(
    private val currencyRateStateGateway: CurrencyRateStateGateway,
    private val defaultFactor: Double
) {

    fun execute(baseCurrency: CurrencyEntity): Completable {
        return currencyRateStateGateway.getBaseCurrency()
            .firstOrError()
            .filter { !it.isSameCurrency(baseCurrency) }
            .flatMapCompletable {
                currencyRateStateGateway.getLastCurrencyRateState()
                    .firstOrError()
                    .map {
                        when (it) {
                            is CurrencyRateState.CurrencyData -> mapCurrencyData(it, baseCurrency)
                            is CurrencyRateState.NotActualCurrencyData -> mapNoActualData(
                                it,
                                baseCurrency
                            )
                            else -> it
                        }
                    }
                    .flatMapCompletable { currencyRateStateGateway.setCurrencyRateState(it) }
            }
            .andThen(currencyRateStateGateway.setBaseCurrency(baseCurrency))
            .andThen(currencyRateStateGateway.setFactor(defaultFactor))
    }

    private fun mapCurrencyData(
        oldData: CurrencyRateState.CurrencyData,
        newBaseCurrency: CurrencyEntity
    ): CurrencyRateState.CurrencyData {
        val newData = mutableSetOf<CurrencyEntity>()

        val newFactor = 1 / newBaseCurrency.value

        newData.add(
            oldData.baseCurrency.copy(
                value = oldData.baseCurrency.value * newFactor,
                multipleValue = oldData.baseCurrency.value * newFactor
            )
        )
        oldData.rates.forEach { currency ->
            if (!currency.isSameCurrency(newBaseCurrency)) {
                val newValue = currency.value * newFactor
                newData.add(
                    currency.copy(value = newValue, multipleValue = newValue)
                )
            }
        }

        return oldData.copy(
            baseCurrency = newBaseCurrency.copy(
                value = defaultFactor,
                multipleValue = defaultFactor
            ),
            rates = newData.sortedBy { it.name }
        )
    }

    private fun mapNoActualData(
        oldData: CurrencyRateState.NotActualCurrencyData,
        newBaseCurrency: CurrencyEntity
    ): CurrencyRateState.NotActualCurrencyData {
        return oldData.copy(lastData = mapCurrencyData(oldData.lastData, newBaseCurrency))
    }
}
