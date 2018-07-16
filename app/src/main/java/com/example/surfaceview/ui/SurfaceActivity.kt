package com.example.surfaceview.ui

import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import kotlinx.android.synthetic.main.activity_surface.*

class SurfaceActivity : BaseActivity() {

    override fun layoutId(): Int = R.layout.activity_surface

    override fun initListener() {
        stop.setOnClickListener {
            surfaceView.stopDraw()
        }

        start.setOnClickListener {
            surfaceView.startDraw()
        }
    }

    override fun initData() {

    }
}
