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
    }

    private val currencyDataGateway = Mockito.mock(CurrencyDataGateway::class.java)
    private val exchangeRatesGateway = Mockito.mock(ExchangeRatesGateway::class.java)

    private val baseCurrency = CurrencyEntity(name = BASE_CURRENCY_NAME)

    @Test
    fun testExchangeRatesInteractor_emptyResponse_emptyCurrencyData() {

        initEmptyCurrencyData()
        initEmptyRatesGateway()

        val getExchangeRatesInteractor = GetExchangeRatesInteractor(exchangeRatesGateway, currencyDataGateway)

        val expectedResult = emptyList<CurrencyEntity>()
        val testObservable = getExchangeRatesInteractor.execute(baseCurrency).test()

        testObservable.assertValue(expectedResult)
    }

    private fun initEmptyCurrencyData() {
        Mockito.`when`(currencyDataGateway.getFlagForCurrency(any())).thenReturn(null)
        Mockito.`when`(currencyDataGateway.getNameForCurrency(any())).thenReturn(null)
    }

    private fun initEmptyRatesGateway() {
        Mockito.`when`(exchangeRatesGateway.getRatesByBaseCurrency(any())).thenReturn(Single.just(emptyList()))
    }
}