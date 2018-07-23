package com.example.surfaceview.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.R
import kotlinx.android.synthetic.main.activity_main_opengl.*

class OpenGLMainActivity:AppCompatActivity() {
    private lateinit var mIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_opengl)

        opengl_01.setOnClickListener {
            mIntent = Intent(this@OpenGLMainActivity, OpenGLES01Activity::class.java)
            startActivity(mIntent)
        }

        opengl_02.setOnClickListener {
            mIntent = Intent(this@OpenGLMainActivity, OpenGLES02Activity::class.java)
            startActivity(mIntent)
        }
    }
}