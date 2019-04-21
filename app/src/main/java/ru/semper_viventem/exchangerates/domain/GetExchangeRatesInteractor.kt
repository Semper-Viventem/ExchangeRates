package ru.semper_viventem.exchangerates.domain

import io.reactivex.Single
import ru.semper_viventem.exchangerates.data.gateway.CurrencyDataGateway
import ru.semper_viventem.exchangerates.data.gateway.ExchangeRatesGateway

class GetExchangeRatesInteractor(
    private val exchangeRatesGateway: ExchangeRatesGateway,
    private val currencyDataGateway: CurrencyDataGateway
) {

    companion object {
        private const val DEFAULT_FACTOR = 1.0
    }

    fun execute(base: CurrencyEntity?): Single<List<CurrencyEntity>> = exchangeRatesGateway.getRatesByBaseCurrency(base)
        .map { sourceCurrencies ->
            val updatedList = mutableListOf<CurrencyEntity>()
            val factor = base?.value ?: DEFAULT_FACTOR

            sourceCurrencies.forEach {
                updatedList.add(
                    it.copy(
                        value = it.value * factor,
                        fullName = currencyDataGateway.getNameForCurrency(it).orEmpty(),
                        image = currencyDataGateway.getFlagForCurrency(it)
                    )
                )
            }

            updatedList
        }
}