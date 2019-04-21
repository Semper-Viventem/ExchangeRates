package ru.semper_viventem.exchangerates.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_currency.view.*
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.extensions.inflate
import ru.semper_viventem.exchangerates.extensions.load

class CurrenciesAdapter(
    private val currencySelected: (currency: CurrencyEntity) -> Unit
) : RecyclerView.Adapter<CurrenciesAdapter.ViewHolder>() {

    private var items: List<CurrencyEntity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_currency))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setData(data: List<CurrencyEntity>) {
        this.items = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        lateinit var item: CurrencyEntity

        init {
            itemView.setOnClickListener { currencySelected.invoke(item) }
        }

        fun bind(currencyEntity: CurrencyEntity) {
            this.item = currencyEntity
            with(itemView) {
                name.text = item.name
                fullName.text = item.fullName
                valueEditText.setText(context.getString(R.string.currency_format, item.value))
                currencyImage.load(item.imageRes, true, R.drawable.currency_placeholder)
            }
        }
    }
}