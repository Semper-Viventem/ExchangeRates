import com.nhaarman.mockito_kotlin.any
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mockito
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.domain.GetExchangeRatesInteractor
import ru.semper_viventem.exchangerates.domain.gateway.CurrencyDataGateway
import ru.semper_viventem.exchangerates.domain.gateway.ExchangeRatesGateway

class GetExchangeRatesInteractor_Test {

    companion object {
        private const val BASE_CURRENCY_NAME = "EUR"
        private const val FLAG_IMAGE_MOCK = "/flag.jpg"
        private const val CURRENCY_FULL_NAME_MOCK = "US Dollar"
    }

    private val currencyDataGateway = Mockito.mock(CurrencyDataGateway::class.java)
    private val exchangeRatesGateway = Mockito.mock(ExchangeRatesGateway::class.java)

    private val getExchangeRatesInteractor = GetExchangeRatesInteractor(exchangeRatesGateway, currencyDataGateway)

    private val initialBaseCurrency = CurrencyEntity(name = BASE_CURRENCY_NAME)
    private val changedBaseCurrency = CurrencyEntity(
        name = BASE_CURRENCY_NAME,
        value = 2.0,
        isBase = true
    )

    private val ratesResponse = listOf(
        CurrencyEntity(
            name = "USD",
            value = 1.0
        ),
        CurrencyEntity(
            name = "AUD",
            value = 2.0
        ),
        CurrencyEntity(
            name = "ZAR",
            value = 3.0
        )
    )

    @Test
    fun testExchangeRatesInteractor_emptyResponse_emptyCurrencyData() {

        initEmptyCurrencyData()
        initEmptyRatesGateway()

        val testObservable = getExchangeRatesInteractor.execute(initialBaseCurrency).test()
        val expectedResult = emptyList<CurrencyEntity>()

        testObservable.assertValue(expectedResult)
    }

    @Test
    fun testExchangeRatesInteractor_response_emptyCurrencyData() {

        initEmptyCurrencyData()
        initExchangeRatesResponse()

        val testObservable = getExchangeRatesInteractor.execute(changedBaseCurrency).test()
        val expectedResult = listOf(
            CurrencyEntity(
                name = BASE_CURRENCY_NAME,
                value = 2.0,
                isBase = true
            ),
            CurrencyEntity(
                name = "USD",
                value = 2.0
            ),
            CurrencyEntity(
                name = "AUD",
                value = 4.0
            ),
            CurrencyEntity(
                name = "ZAR",
                value = 6.0
            )
        )

        testObservable.assertValue(expectedResult)
    }

    @Test
    fun testExchangeRatesInteractor_response_currencyData() {

        initCurrencyData()
        initExchangeRatesResponse()

        val testObservable = getExchangeRatesInteractor.execute(changedBaseCurrency).test()
        val expectedResult = listOf(
            CurrencyEntity(
                name = BASE_CURRENCY_NAME,
                value = 2.0,
                isBase = true,
                fullName = CURRENCY_FULL_NAME_MOCK,
                image = FLAG_IMAGE_MOCK
            ),
            CurrencyEntity(
                name = "USD",
                value = 2.0,
                fullName = CURRENCY_FULL_NAME_MOCK,
                image = FLAG_IMAGE_MOCK
            ),
            CurrencyEntity(
                name = "AUD",
                value = 4.0,
                fullName = CURRENCY_FULL_NAME_MOCK,
                image = FLAG_IMAGE_MOCK
            ),
            CurrencyEntity(
                name = "ZAR",
                value = 6.0,
                fullName = CURRENCY_FULL_NAME_MOCK,
                image = FLAG_IMAGE_MOCK
            )
        )

        testObservable.assertValue(expectedResult)
    }

    private fun initEmptyCurrencyData() {
        Mockito.`when`(currencyDataGateway.getFlagForCurrency(any())).thenReturn(null)
        Mockito.`when`(currencyDataGateway.getNameForCurrency(any())).thenReturn(null)
    }

    private fun initEmptyRatesGateway() {
        Mockito.`when`(exchangeRatesGateway.getRatesByBaseCurrency(any())).thenReturn(Single.just(emptyList()))
    }

    private fun initExchangeRatesResponse() {
        val exchangeRatesResponse = listOf(changedBaseCurrency) + ratesResponse

        Mockito.`when`(exchangeRatesGateway.getRatesByBaseCurrency(any()))
            .thenReturn(Single.just(exchangeRatesResponse))
    }

    private fun initCurrencyData() {
        Mockito.`when`(currencyDataGateway.getFlagForCurrency(any())).thenReturn(FLAG_IMAGE_MOCK)
        Mockito.`when`(currencyDataGateway.getNameForCurrency(any())).thenReturn(CURRENCY_FULL_NAME_MOCK)
    }
}