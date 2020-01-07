package ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.data.gateway.CurrencyRateStateImpl
import ru.semper_viventem.exchangerates.data.gateway.ExchangeRatesGatewayImpl
import ru.semper_viventem.exchangerates.data.network.Api
import ru.semper_viventem.exchangerates.data.network.response.AllRatesResponse
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway
import ru.semper_viventem.exchangerates.ui.MainActivity
import java.net.UnknownHostException

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : KoinTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    private val defaultServerResponse = ExchangeRatesResponse(
        base = "EUR",
        date = "2018-09-06",
        ratesResponse = AllRatesResponse(
            rates = listOf(
                "USD" to 1.0,
                "RUB" to 2.0
            )
        )
    )
    private val baseCurrency = CurrencyEntity("EUR")

    private val fakeApi = FakeApi()

    @Before
    fun setUp() {
        loadKoinModules(module {
            factory(override = true) { fakeApi } bind Api::class
            factory(override = true) { ExchangeRatesGatewayImpl(get()) } bind ExchangeRatesGateway::class
            factory(override = true) {
                CurrencyRateStateImpl(baseCurrency, 1.0)
            } bind CurrencyRateStateGateway::class
        })
    }

    @Test
    fun testDataInLoading() {
        activityRule.launchActivity(null)
        Thread.sleep(1000L)
        onView(withId(R.id.progress))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDataWasLoadedAndShownInList() {
        fakeApi.result = Single.just(defaultServerResponse)
        activityRule.launchActivity(null)

        Thread.sleep(1000L)
        onView(withId(R.id.recyclerView))
            .check(matches(hasChildCount(3)))
    }

    @Test
    fun testShowErrorMessageIfNoInternet() {
        fakeApi.result = Single.error(UnknownHostException("host"))
        activityRule.launchActivity(null)

        Thread.sleep(1000L)
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.error_failed)))
    }

    private class FakeApi : Api {

        var result: Single<ExchangeRatesResponse> = Single.create { }

        override fun latest(base: String?): Single<ExchangeRatesResponse> {
            return result
        }
    }
}
