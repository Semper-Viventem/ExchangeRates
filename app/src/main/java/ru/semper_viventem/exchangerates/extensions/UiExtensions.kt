package ru.semper_viventem.exchangerates.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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