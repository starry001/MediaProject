package com.example.surfaceview.util

object JNIUtils {

    init {
        System.loadLibrary("opengl_es")
    }

    external fun stringFromJNI(): String
}