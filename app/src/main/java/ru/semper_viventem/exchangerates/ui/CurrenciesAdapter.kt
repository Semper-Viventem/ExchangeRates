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

    var items: List<CurrencyEntity> = listOf()
    private var needToShowKeyboard: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_currency))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setData(data: List<CurrencyEntity>, refreshBaseCurrency: Boolean) {
        if (refreshBaseCurrency) {
            updateDataAndRefreshBaseCurrency(data)
        } else {
            updateAllData(data)
        }
    }

    private fun updateDataAndRefreshBaseCurrency(data: List<CurrencyEntity>) {
        needToShowKeyboard = true
        val diffUtil = DiffUtil.calculateDiff(DiffUtilCallback(items, data))
        items = data
        diffUtil.dispatchUpdatesTo(this)
        notifyItemChanged(0)
    }

    private fun updateAllData(data: List<CurrencyEntity>) {
        val refreshAllData = items.isEmpty()
        items = data

        if (refreshAllData) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(1, items.size - 1)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var item: CurrencyEntity

        private val isFirstElement get() = adapterPosition == 0
        private val changedListener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (isFirstElement) {
                    baseValueChangeListener.invoke(text.toString())
                }
            }

        }

        init {
            itemView.setOnClickListener { currencySelected.invoke(item) }
            itemView.valueEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    currencySelected.invoke(item)
                }
            }
        }

        fun bind(currencyEntity: CurrencyEntity) {
            this.item = currencyEntity
            with(itemView) {
                name.text = item.name
                fullName.text = item.fullName
                valueEditText.setText(context.getString(R.string.currency_format, item.multipleValue))
                currencyImage.load(item.image, true, R.drawable.currency_placeholder)
                if (needToShowKeyboard && isFirstElement) {
                    valueEditText.showKeyboard()
                    needToShowKeyboard = false
                }
                if (isFirstElement) {
                    valueEditText.addTextChangedListener(changedListener)
                } else {
                    valueEditText.removeTextChangedListener(changedListener)
                }
            }
        }
    }

    private class DiffUtilCallback(
        private val oldItems: List<CurrencyEntity>,
        private val newItems: List<CurrencyEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition].isSameCurrency(newItems[newItemPosition])

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition].isSameCurrency(newItems[newItemPosition])

    }
}
