package com.example.surfaceview.ui

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.opengl.My02GLRenderer

//坐标变换
class OpenGLES02Activity : AppCompatActivity() {

    private val mSurfaceView: GLSurfaceView by lazy {
        GLSurfaceView(this@OpenGLES02Activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSurfaceView.setRenderer(My02GLRenderer())
        setContentView(mSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        mSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mSurfaceView.onPause()
    }
}