package ru.semper_viventem.exchangerates.ui

import org.koin.dsl.module

object UIModule {
    val module = module {
        factory { MainPm() }
    }
}