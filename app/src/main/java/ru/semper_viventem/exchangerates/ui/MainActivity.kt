package ru.semper_viventem.exchangerates.ui

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import me.dmdev.rxpm.base.PmSupportActivity
import org.koin.android.ext.android.getKoin
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.domain.CurrencyRateState
import ru.semper_viventem.exchangerates.extensions.hideKeyboard
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class MainActivity : PmSupportActivity<MainPm>() {

    companion object {
        private const val MIN_SCROLL_DIFF_FOR_HIDE_KEYBOARD = 10
    }

    override fun providePresentationModel(): MainPm = getKoin().get()

    private val currenciesAdapter = CurrenciesAdapter(
        currencySelected = { currency ->
            currency passTo presentationModel.currencySelected.consumer
        },
        baseValueChangeListener = { text ->
            text passTo presentationModel.factorInput.consumer
        }
    )

    private lateinit var snackbar: Snackbar
    private var inScroll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        snackbar = Snackbar.make(container, "", Snackbar.LENGTH_INDEFINITE)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = currenciesAdapter
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    inScroll = newState != RecyclerView.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy >= MIN_SCROLL_DIFF_FOR_HIDE_KEYBOARD) hideKeyboard()
                }
            })
        }
    }

    override fun onBindPresentationModel(pm: MainPm) {
        pm.viewState bindTo ::render
    }

    private fun render(state: CurrencyRateState) {

        Timber.d("UI State: $state")

        when (state) {
            is CurrencyRateState.NoData -> showNoDataState()
            is CurrencyRateState.CurrencyData -> showDataState(state)
            is CurrencyRateState.LoadingError -> showErrorState()
            is CurrencyRateState.NotActualCurrencyData -> showNotActualDataState(state)
        }
    }

    private fun showNoDataState() {
        progress.isVisible = true
        recyclerView.isVisible = false
        hideError()
    }

    private fun showDataState(state: CurrencyRateState.CurrencyData) {
        progress.isVisible = false
        recyclerView.isVisible = true
        hideError()
        refreshRatesList(state)
    }

    private fun showErrorState() {
        progress.isVisible = true
        recyclerView.isVisible = false
        showErrorMessage(getString(R.string.error_failed))
    }

    private fun showNotActualDataState(state: CurrencyRateState.NotActualCurrencyData) {
        progress.isVisible = false
        recyclerView.isVisible = true
        refreshRatesList(state.lastData)

        val lastUpdateSeconds =
            MILLISECONDS.toSeconds(Date().time - state.lastData.lastUpdateTime.time)

        val (timeUnit, timeUnitStr) = when (lastUpdateSeconds) {
            1L -> lastUpdateSeconds to getString(R.string.second)
            in 2L..59L -> lastUpdateSeconds to getString(R.string.seconds)
            in 60L..69L -> SECONDS.toMinutes(lastUpdateSeconds) to getString(R.string.minute)
            else -> SECONDS.toMinutes(lastUpdateSeconds) to getString(R.string.minutes)
        }

        showErrorMessage(getString(R.string.error_failed_with_data, timeUnit, timeUnitStr))
    }

    private fun refreshRatesList(data: CurrencyRateState.CurrencyData) {
        if (inScroll) return

        val items = listOf(data.baseCurrency) + data.rates
        val needToRefreshBaseCurrency =
            currenciesAdapter.items.firstOrNull()?.isSameCurrency(data.baseCurrency) != true

        if (needToRefreshBaseCurrency) {
            recyclerView.scrollToPosition(0)
        }

        currenciesAdapter.setData(items, needToRefreshBaseCurrency)
    }

    private fun showErrorMessage(message: String) {
        with(snackbar) {
            setText(message)
            show()
        }
    }

    private fun hideError() {
        snackbar.dismiss()
    }
}
