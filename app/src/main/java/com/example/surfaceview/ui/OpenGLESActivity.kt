package com.example.surfaceview.ui

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.opengl.MyGLSurfaceView

class OpenGLESActivity : AppCompatActivity() {
    private var mGLView: GLSurfaceView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLView = MyGLSurfaceView(this)
        setContentView(mGLView!!)

    }

    override fun onPause() {
        super.onPause()
        mGLView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mGLView?.onResume()
    }
}
