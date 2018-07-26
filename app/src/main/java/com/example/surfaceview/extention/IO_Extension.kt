package com.example.surfaceview.extention

import java.io.Closeable
import java.io.IOException

fun Closeable.closeQuiety() {
    try {
        close()
    } catch (e: IOException) {
        logger("close exception ${e.message}")
    }
}

