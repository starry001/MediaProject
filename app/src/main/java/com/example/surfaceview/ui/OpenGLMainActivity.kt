package com.example.surfaceview.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.R
import com.example.surfaceview.extention.jump
import kotlinx.android.synthetic.main.activity_main_opengl.*

class OpenGLMainActivity:AppCompatActivity() {
    private lateinit var mIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_opengl)

        opengl_01.setOnClickListener {
            jump<OpenGLES01Activity>()
        }

        opengl_02.setOnClickListener {
            jump<OpenGLES02Activity>()
        }
    }
}