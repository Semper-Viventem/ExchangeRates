package ru.semper_viventem.exchangerates.extensions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun ImageView.load(
    res: Any?,
    circular: Boolean = false,
    @DrawableRes placeholder: Int? = null
) {
    Glide.with(this)
        .load(res)
        .apply(RequestOptions().apply {
            if (circular) {
                circleCrop()
            }
            if (placeholder != null) {
                placeholder(placeholder)
            }
        })
        .into(this)
}

fun View.showKeyboard() {
    val function = {
        if (requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, 0)
        }
    }

    function.invoke()
    post {
        function.invoke()
    }
}

fun View.hideKeyboard() {
    val function = {
        clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    function.invoke()
    post {
        function.invoke()
    }
}

fun View.visible(visible: Boolean, useGone: Boolean = true) {
    this.visibility = if (visible) View.VISIBLE else if (useGone) View.GONE else View.INVISIBLE
}