package ru.semper_viventem.exchangerates.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_main.*
import me.dmdev.rxpm.base.PmSupportActivity
import org.koin.android.ext.android.getKoin
import ru.semper_viventem.exchangerates.R
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
            text passTo presentationModel.baseCurrencyInput.consumer
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = currenciesAdapter
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val inScroll = newState != RecyclerView.SCROLL_STATE_IDLE
                    inScroll passTo presentationModel.changeScrollState.consumer
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy >= MIN_SCROLL_DIFF_FOR_HIDE_KEYBOARD) {
                        hideKeyboard()
                    }
                }
            })
        }
    }

    override fun onBindPresentationModel(pm: MainPm) {
        pm.rateAndUpdateTopItem bindTo { (rates, updateTopItem) ->
            currenciesAdapter.setData(rates, updateTopItem)
            if (updateTopItem) {
                recyclerView.scrollToPosition(0)
            }
        }
    }
}
