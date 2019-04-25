import org.junit.Assert
import org.junit.Test
import ru.semper_viventem.exchangerates.data.mapToCurrenciesList
import ru.semper_viventem.exchangerates.data.network.response.AllRatesResponse
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.domain.CurrencyEntity


class ExchangeRatesMapperTest {

    companion object {
        private const val BASE_CURRENCY = "EUR"
        private const val DATE = "04.24.2017"
        private const val RATE_NAME = "USD"
        private const val RATE_VALUE = 1.0
    }

    private val response = ExchangeRatesResponse(
        base = BASE_CURRENCY,
        date = DATE,
        ratesResponse = AllRatesResponse(
            rates = listOf(
                RATE_NAME to RATE_VALUE,
                RATE_NAME to RATE_VALUE,
                RATE_NAME to RATE_VALUE
            )
        )
    )

    @Test
    fun testMappingCurrencyResponse_baseIsNotNull() {
        val baseCurrency = CurrencyEntity("RUB", isBase = true)

        val expectedResult = listOf(
            baseCurrency,
            CurrencyEntity(name = RATE_NAME),
            CurrencyEntity(name = RATE_NAME),
            CurrencyEntity(name = RATE_NAME)
        )

        val actualResult = response.mapToCurrenciesList(baseCurrency)

        Assert.assertEquals(expectedResult, actualResult)
    }

    @Test
    fun testMappingCurrencyResponse_baseIsNull() {
        val expectedResult = listOf(
            CurrencyEntity(name = BASE_CURRENCY, isBase = true),
            CurrencyEntity(name = RATE_NAME),
            CurrencyEntity(name = RATE_NAME),
            CurrencyEntity(name = RATE_NAME)
        )

        val actualResult = response.mapToCurrenciesList(null)

        Assert.assertEquals(expectedResult, actualResult)
    }

    @Test
    fun testMappingCurrencyResponse_ratesIsEmpty() {
        val response = ExchangeRatesResponse(
            base = BASE_CURRENCY,
            date = DATE,
            ratesResponse = AllRatesResponse(
                rates = listOf()
            )
        )

        val expectedResult = listOf(
            CurrencyEntity(name = BASE_CURRENCY, isBase = true)
        )

        val actualResult = response.mapToCurrenciesList(null)

        Assert.assertEquals(expectedResult, actualResult)
    }
}