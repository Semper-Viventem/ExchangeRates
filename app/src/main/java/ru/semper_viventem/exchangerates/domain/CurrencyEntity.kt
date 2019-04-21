package ru.semper_viventem.exchangerates.domain

data class CurrencyEntity(
    val name: String,
    val fullName: String = "",
    val value: Double = DEFAULT_CURRENCY_VALUE,
    val image: String? = null,
    val isBase: Boolean = false
) {

    companion object {
        const val DEFAULT_CURRENCY_VALUE = 1.0
    }

    fun isSameCurrency(otherCurrency: CurrencyEntity): Boolean {
        return this.name == otherCurrency.name
    }
}