package ru.semper_viventem.exchangerates.domain.gateway

import ru.semper_viventem.exchangerates.domain.CurrencyEntity

interface CurrencyDetailsGateway {

    fun getNameForCurrency(currency: CurrencyEntity): String?

    fun getFlagForCurrency(currency: CurrencyEntity): String?
}
