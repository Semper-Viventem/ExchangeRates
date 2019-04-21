package ru.semper_viventem.exchangerates.domain

data class CurrencyEntity(
    val name: String,
    val fullName: String,
    val value: Double,
    val image: String?
) {
    fun isSameCurrency(otherCurrency: CurrencyEntity): Boolean {
        return this.name == otherCurrency.name
    }
}