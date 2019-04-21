package ru.semper_viventem.exchangerates.data.gateway

import android.content.Context
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import timber.log.Timber

class CurrencyDataGateway(
    private val context: Context
) {

    companion object {
        private const val FLAGS_PATCH = "flags"
        private const val ASSET_PATCH = "file:///android_asset"
    }

    private val flags = mutableMapOf<String, String>()

    private val currencyNames = mapOf(
        "USD" to R.string.usd,
        "GBP" to R.string.gbp,
        "JPY" to R.string.jby,
        "CAD" to R.string.cad,
        "CZK" to R.string.czk,
        "AUD" to R.string.aud,
        "EUR" to R.string.eur
    )

    init {
        val am = context.assets
        am.list(FLAGS_PATCH)?.forEach { fileName ->
            Timber.d("Asset name: $fileName")
            val key = fileName.split(".")[0]
            flags[key] = "$ASSET_PATCH/$FLAGS_PATCH/$fileName"
        }
    }

    fun getNameForCurrency(currency: CurrencyEntity): String? {
        val currencyRes = currencyNames[currency.name]
        return currencyRes?.let { context.getString(currencyRes) }
    }

    fun getFlagForCurrency(currency: CurrencyEntity): String? {
        return flags[currency.name.toLowerCase()]
    }
}