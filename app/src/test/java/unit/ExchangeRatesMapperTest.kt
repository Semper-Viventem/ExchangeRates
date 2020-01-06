package unit

import org.junit.Assert
import org.junit.Test
import ru.semper_viventem.exchangerates.data.network.response.AllRatesResponse
import ru.semper_viventem.exchangerates.data.network.response.ExchangeRatesResponse
import ru.semper_viventem.exchangerates.data.toCurrenciesList
import ru.semper_viventem.exchangerates.domain.CurrencyEntity


class ExchangeRatesMapperTest {

    companion object {
        private const val BASE_CURRENCY = "EUR"
        private const val DATE = "04.24.2017"
        private const val RATE_NAME = "USD"
        private const val RATE_VALUE = 1.0
    }

    @Test
    fun `test map if data is not empty`() {
        val response = ExchangeRatesResponse(
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

        val expectedResult = listOf(
            CurrencyEntity(name = RATE_NAME, value = RATE_VALUE),
            CurrencyEntity(name = RATE_NAME, value = RATE_VALUE),
            CurrencyEntity(name = RATE_NAME, value = RATE_VALUE)
        )

        val actualResult = response.toCurrenciesList()

        Assert.assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `test map if data is empty`() {
        val response = ExchangeRatesResponse(
            base = BASE_CURRENCY,
            date = DATE,
            ratesResponse = AllRatesResponse(
                rates = emptyList()
            )
        )

        val expectedResult = emptyList<CurrencyEntity>()

        val actualResult = response.toCurrenciesList()

        Assert.assertEquals(expectedResult, actualResult)
    }
}
