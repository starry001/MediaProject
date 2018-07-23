package com.example.surfaceview.util

object JNIUtils {

    external fun stringFromJNI(): String

    init {
        System.loadLibrary("opengl_es")
    }
}