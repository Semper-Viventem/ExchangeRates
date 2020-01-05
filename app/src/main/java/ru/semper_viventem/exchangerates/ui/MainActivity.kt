package ru.semper_viventem.exchangerates.ui

import android.os.Bundle
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
        })

    private lateinit var errorSnackbar: Snackbar
    private var inScroll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        errorSnackbar = Snackbar.make(container, getString(R.string.error_no_internet_connection), Snackbar.LENGTH_INDEFINITE)

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
        // TODO: Draw state
    }
}
