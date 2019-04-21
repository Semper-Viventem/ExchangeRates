package ru.semper_viventem.exchangerates.domain

import io.reactivex.Single
import ru.semper_viventem.exchangerates.data.gateway.CurrencyDataGateway
import ru.semper_viventem.exchangerates.data.gateway.ExchangeRatesGateway

class GetExchangeRatesInteractor(
    private val exchangeRatesGateway: ExchangeRatesGateway,
    private val currencyDataGateway: CurrencyDataGateway
) {

    fun execute(base: CurrencyEntity?): Single<List<CurrencyEntity>> = exchangeRatesGateway.getRatesByBaseCurrency(base)
        .map { sourceCurrencies ->
            val updatedList = mutableListOf<CurrencyEntity>()
            val factor = base?.value ?: CurrencyEntity.DEFAULT_CURRENCY_VALUE

            sourceCurrencies.forEach {

                val multipliedValue = if (base?.isSameCurrency(it) == true) {
                    it.value
                } else {
                    it.value * factor
                }

                updatedList.add(
                    it.copy(
                        value = multipliedValue,
                        fullName = currencyDataGateway.getNameForCurrency(it).orEmpty(),
                        image = currencyDataGateway.getFlagForCurrency(it)
                    )
                )
            }

            updatedList
        }
}