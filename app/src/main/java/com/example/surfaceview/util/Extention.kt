package com.example.surfaceview.util

import android.app.Activity
import android.util.Log
import android.widget.Toast


fun Activity.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Activity.logger(tag: String, msg: String?) {
    Log.e(tag, msg)
}


fun Activity.logger(msg: String?) {
    Log.e("logger", msg)
}

fun Any.logger(msg: String?) {
    Log.e("logger", msg)
}

