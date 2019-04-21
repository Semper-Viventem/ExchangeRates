package ru.semper_viventem.exchangerates.data.gateway

import android.content.Context
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.domain.CurrencyEntity

class CurrencyDataGateway(
    private val context: Context
) {

    private val currencyNames = mapOf(
        "USD" to R.string.usd,
        "GBP" to R.string.gbp,
        "JPY" to R.string.jby,
        "CAD" to R.string.cad,
        "CZK" to R.string.czk,
        "AUD" to R.string.aud,
        "EUR" to R.string.eur
    )

    fun getNameForCurrency(currency: CurrencyEntity): String? {
        val currencyRes = currencyNames[currency.name]
        return currencyRes?.let { context.getString(currencyRes) }
    }
}