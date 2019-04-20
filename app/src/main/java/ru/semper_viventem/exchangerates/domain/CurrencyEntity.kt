package ru.semper_viventem.exchangerates.domain

data class CurrencyEntity(
    val name: String,
    val fullName: String,
    val value: Double,
    val imageRes: Int
)