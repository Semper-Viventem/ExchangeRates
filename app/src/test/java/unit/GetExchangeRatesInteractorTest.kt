package unit

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import ru.semper_viventem.exchangerates.data.gateway.CurrencyRateStateImpl
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDetailsGateway
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyRateStateGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway
import ru.semper_viventem.exchangerates.domain.interactor.GetExchangeRatesInteractor
import java.net.UnknownHostException
import java.util.*

class GetExchangeRatesInteractorTest {

    private val baseCurrency = CurrencyEntity("EUR")
    private val defaultFactor = 1.0

    @Test
    fun `test no data`() {

        val exchangeRatesGateway = getExchangeRateGatewayWithDefault(listOf())
        val currencyDetailsGateway = getCurrencyDetailsGatewayWithDefault("", "")
        val currencyRateStateGateway = CurrencyRateStateImpl(baseCurrency, defaultFactor)

        val interactor = GetExchangeRatesInteractor(
            exchangeRatesGateway,
            currencyDetailsGateway,
            currencyRateStateGateway
        )

        interactor.execute().test()
            .assertValueAt(0) { it == CurrencyRateState.NoData }
    }

    @Test
    fun `test data is exist`() {

        val items = listOf(
            CurrencyEntity(name = "USD", value = 1.0),
            CurrencyEntity(name = "USD", value = 2.0),
            CurrencyEntity(name = "USD", value = 3.0)
        )

        val exchangeRatesGateway = getExchangeRateGatewayWithDefault(items)
        val currencyDetailsGateway = getCurrencyDetailsGatewayWithDefault("name", "image")
        val currencyRateStateGateway = CurrencyRateStateImpl(baseCurrency, defaultFactor)

        val interactor = GetExchangeRatesInteractor(
            exchangeRatesGateway,
            currencyDetailsGateway,
            currencyRateStateGateway
        )

        interactor.execute().test()
            .assertValueAt(0) { it == CurrencyRateState.NoData }
            .assertValueAt(1) { it is CurrencyRateState.CurrencyData }
            .assertValueAt(1) {
                val expected = CurrencyRateState.CurrencyData(
                    baseCurrency = baseCurrency.copy(
                        fullName = "name",
                        image = "image"
                    ),
                    rates = listOf(
                        CurrencyEntity(
                            name = "USD",
                            value = 1.0,
                            multipleValue = 1.0,
                            fullName = "name",
                            image = "image"
                        ),
                        CurrencyEntity(
                            name = "USD",
                            value = 2.0,
                            multipleValue = 2.0,
                            fullName = "name",
                            image = "image"
                        ),
                        CurrencyEntity(
                            name = "USD",
                            value = 3.0,
                            multipleValue = 3.0,
                            fullName = "name",
                            image = "image"
                        )
                    ),
                    lastUpdateTime = (it as CurrencyRateState.CurrencyData).lastUpdateTime
                )

                it == expected
            }
    }

    @Test
    fun `test data not actual`() {

        val items = listOf(
            CurrencyEntity(name = "USD", value = 1.0),
            CurrencyEntity(name = "USD", value = 2.0),
            CurrencyEntity(name = "USD", value = 3.0)
        )

        val time = Date()
        val lastState = CurrencyRateState.CurrencyData(
            baseCurrency = baseCurrency,
            rates = items,
            lastUpdateTime = time
        )
        val error = UnknownHostException("host")

        val exchangeRatesGateway = getExchangeRateGatewayWithError(error)
        val currencyDetailsGateway = getCurrencyDetailsGatewayWithDefault("name", "image")

        val currencyRateStateGateway: CurrencyRateStateGateway = mock {
            on { getLastCurrencyRateState() } doReturn Observable.just(lastState as CurrencyRateState)
            on { getFactor() } doReturn Observable.just(1.0)
            on { getBaseCurrency() } doReturn Observable.just(baseCurrency)
            on { setCurrencyRateState(any()) } doReturn Completable.complete()
        }

        val interactor = GetExchangeRatesInteractor(
            exchangeRatesGateway,
            currencyDetailsGateway,
            currencyRateStateGateway
        )

        interactor.execute().test()
            .assertValueAt(0) { it is CurrencyRateState.CurrencyData }
            .assertValueAt(1) { it is CurrencyRateState.NotActualCurrencyData }
            .assertValueAt(1) {
                val expected = CurrencyRateState.NotActualCurrencyData(
                    error = error,
                    lastData = lastState.copy(
                        rates = listOf(
                            CurrencyEntity(
                                name = "USD",
                                value = 1.0,
                                fullName = "name",
                                image = "image",
                                multipleValue = 1.0
                            ),
                            CurrencyEntity(
                                name = "USD",
                                value = 2.0,
                                fullName = "name",
                                image = "image",
                                multipleValue = 2.0
                            ),
                            CurrencyEntity(
                                name = "USD",
                                value = 3.0,
                                fullName = "name",
                                image = "image",
                                multipleValue = 3.0
                            )
                        )
                    )
                )
                it == expected
            }
    }

    @Test
    fun `test loading error`() {

        val error = UnknownHostException("host")

        val exchangeRatesGateway = getExchangeRateGatewayWithError(error)
        val currencyDetailsGateway = getCurrencyDetailsGatewayWithDefault("name", "image")
        val currencyRateStateGateway = CurrencyRateStateImpl(baseCurrency, defaultFactor)

        val interactor = GetExchangeRatesInteractor(
            exchangeRatesGateway,
            currencyDetailsGateway,
            currencyRateStateGateway
        )

        interactor.execute().test()
            .assertValueAt(0) { it == CurrencyRateState.NoData }
            .assertValueAt(1) { it == CurrencyRateState.LoadingError(error) }
    }

    private fun getExchangeRateGatewayWithDefault(items: List<CurrencyEntity>): ExchangeRatesGateway {
        return mock {
            on { getRatesByBaseCurrency(any()) } doReturn Single.fromCallable { items }
        }
    }

    private fun getExchangeRateGatewayWithError(e: Throwable): ExchangeRatesGateway {
        return mock {
            on { getRatesByBaseCurrency(any()) } doReturn Single.error(e)
        }
    }

    private fun getCurrencyDetailsGatewayWithDefault(
        name: String,
        image: String
    ): CurrencyDetailsGateway {
        return mock {
            on { getNameForCurrency(any()) } doReturn name
            on { getFlagForCurrency(any()) } doReturn image
        }
    }
}
