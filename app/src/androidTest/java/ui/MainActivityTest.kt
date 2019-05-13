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
import ru.semper_viventem.exchangerates.data.network.Api
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.ui.MainActivity
import java.net.UnknownHostException

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : KoinTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    class FakeApi : Api {
        override fun latest(base: String?): Single<ExchangeRatesResponse> {
            val networkErrorResponse = Single.error<ExchangeRatesResponse>(UnknownHostException())
            return networkErrorResponse
        }

    }

    @Before
    fun setUp() {
        loadKoinModules(module {
            single(override = true) { FakeApi() } bind Api::class
        })
    }

    @Test
    fun testDataInLoading() {
        activityRule.launchActivity(null)
        onView(withId(R.id.progress))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testErrorMessageIfNoInternet() {
        activityRule.launchActivity(null)
        Thread.sleep(1000L)
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.error_no_internet_connection)))
    }
}