package com.example.surfaceview.extention

import android.util.Log

fun <T : Any> T.logger(tag: String, msg: String?) {
    Log.e(tag, msg)
}

fun <T : Any> T.logger(msg: String?) {
    Log.e(this::class.java.simpleName, msg)
}

