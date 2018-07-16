package com.example.surfaceview.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(layoutId())

        initData()
        initListener()
    }

    abstract fun layoutId(): Int

    abstract fun initListener()

    abstract fun initData()
}