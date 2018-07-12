package com.example.surfaceview.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.R
import kotlinx.android.synthetic.main.activity_surface.*

class SurfaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface)

        stop.setOnClickListener {
            surfaceView.stopDraw()
        }

        start.setOnClickListener {
            surfaceView.startDraw()
        }
    }
}
