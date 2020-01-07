package ru.semper_viventem.exchangerates.domain

import java.util.*

sealed class CurrencyRateState {

    object NoData : CurrencyRateState()

    data class LoadingError(
        val error: Throwable
    ) : CurrencyRateState()

    data class CurrencyData(
        val baseCurrency: CurrencyEntity,
        val rates: List<CurrencyEntity>,
        val lastUpdateTime: Date
    ) : CurrencyRateState()

    data class NotActualCurrencyData(
        val error: Throwable,
        val lastData: CurrencyData
    ) : CurrencyRateState()

}
