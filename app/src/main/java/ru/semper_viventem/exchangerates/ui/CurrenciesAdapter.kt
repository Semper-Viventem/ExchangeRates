package ru.semper_viventem.exchangerates.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_currency.view.*
import ru.semper_viventem.exchangerates.R
import ru.semper_viventem.exchangerates.domain.CurrencyEntity
import ru.semper_viventem.exchangerates.extensions.inflate
import ru.semper_viventem.exchangerates.extensions.load
import ru.semper_viventem.exchangerates.extensions.showKeyboard

class CurrenciesAdapter(
    private val currencySelected: (currency: CurrencyEntity) -> Unit,
    private val baseValueChangeListener: (text: String) -> Unit
) : RecyclerView.Adapter<CurrenciesAdapter.ViewHolder>() {

    private var items: List<MainPm.CurrencyListItem> = listOf()
    private var needToShowKeyboard: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_currency))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setData(data: List<MainPm.CurrencyListItem>, refreshBaseCurrency: Boolean) {
        if (refreshBaseCurrency) {
            updateDataAndRefreshBaseCurrency(data)
        } else {
            updateAllData(data)
        }
    }

    private fun updateDataAndRefreshBaseCurrency(data: List<MainPm.CurrencyListItem>) {
        needToShowKeyboard = true
        val diffUtil = DiffUtil.calculateDiff(DiffUtilCallback(items, data))
        items = data
        diffUtil.dispatchUpdatesTo(this)
        notifyItemChanged(0)
    }

    private fun updateAllData(data: List<MainPm.CurrencyListItem>) {
        val refreshAllData = items.isEmpty()
        items = data

        if (refreshAllData) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(1, items.size - 1)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var item: MainPm.CurrencyListItem

        init {
            itemView.setOnClickListener { currencySelected.invoke(item.currency) }
            itemView.valueEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    currencySelected.invoke(item.currency)
                }
            }
            itemView.valueEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // do nothing
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // do nothing
                }

                override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                    if (item.isBaseCurrency) {
                        baseValueChangeListener.invoke(text.toString())
                    }
                }

            })
        }

        fun bind(currencyEntity: MainPm.CurrencyListItem) {
            this.item = currencyEntity
            with(itemView) {
                name.text = item.currency.name
                fullName.text = item.currency.fullName
                valueEditText.setText(context.getString(R.string.currency_format, item.currency.value))
                currencyImage.load(item.currency.image, true, R.drawable.currency_placeholder)
                if (needToShowKeyboard && item.isBaseCurrency) {
                    valueEditText.showKeyboard()
                    needToShowKeyboard = false
                }
            }
        }
    }

    private class DiffUtilCallback(
        private val oldItems: List<MainPm.CurrencyListItem>,
        private val newItems: List<MainPm.CurrencyListItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition].currency.isSameCurrency(newItems[newItemPosition].currency)

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition].currency.isSameCurrency(newItems[newItemPosition].currency)

    }
}