package ru.semper_viventem.exchangerates.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import me.dmdev.rxpm.base.PmSupportActivity
import org.koin.android.ext.android.getKoin
import ru.semper_viventem.exchangerates.R

class MainActivity : PmSupportActivity<MainPm>() {

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
