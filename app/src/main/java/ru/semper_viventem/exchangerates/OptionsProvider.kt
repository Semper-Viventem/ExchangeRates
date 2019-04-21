package ru.semper_viventem.exchangerates

import ru.semper_viventem.exchangerates.domain.CurrencyEntity

object OptionsProvider {

    val BASE_CURRENCY = CurrencyEntity(
        name = "EUR",
        fullName = "Euro",
        value = 1.0,
        image = "file:///android_asset/flags/eur.png"
    )
}