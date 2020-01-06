package ru.semper_viventem.exchangerates.data.gateway

import android.content.Context
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDetailsGateway
import timber.log.Timber

class CurrencyDetailsGatewayImpl(
    private val context: Context
): CurrencyDetailsGateway {

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
        "EUR" to R.string.eur,
        "CHF" to R.string.chf,
        "AFN" to R.string.afn,
        "ALL" to R.string.all,
        "BGN" to R.string.bgn,
        "BRL" to R.string.brl,
        "CNY" to R.string.cny,
        "DKK" to R.string.dkk,
        "HKD" to R.string.hkd,
        "HRK" to R.string.hrk,
        "HUF" to R.string.huf
    )

    init {
        val am = context.assets
        am.list(FLAGS_PATCH)?.forEach { fileName ->
            Timber.d("Asset name: $fileName")
            val key = fileName.split(".")[0]
            flags[key] = "$ASSET_PATCH/$FLAGS_PATCH/$fileName"
        }
    }

    override fun getNameForCurrency(currency: CurrencyEntity): String? {
        val currencyRes = currencyNames[currency.name]
        return currencyRes?.let { context.getString(currencyRes) }
    }

    override fun getFlagForCurrency(currency: CurrencyEntity): String? {
        return flags[currency.name.toLowerCase()]
    }
}
