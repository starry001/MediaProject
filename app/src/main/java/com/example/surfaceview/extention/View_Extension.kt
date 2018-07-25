package com.example.surfaceview.extention

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

fun ViewGroup.inflate(@LayoutRes layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}

val View.isVisible: Boolean
    get() = visibility == View.VISIBLE
val View.isInvisible: Boolean
    get() = visibility == View.INVISIBLE
val View.isGone: Boolean
    get() = visibility == View.GONE

fun View.hideKeyboard(): Boolean {
    clearFocus()
    return (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard(): Boolean {
    requestFocus()
    return (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}