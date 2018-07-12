package com.example.surfaceview.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.R
import kotlinx.android.synthetic.main.activity_index.*

class IndexActivity : AppCompatActivity() {
    private lateinit var mIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        surface.setOnClickListener {
            mIntent = Intent(this@IndexActivity, SurfaceActivity::class.java)
            startActivity(mIntent)
        }

        audio.setOnClickListener {
            mIntent = Intent(this@IndexActivity, AudioActivity::class.java)
            startActivity(mIntent)
        }

        camera.setOnClickListener {
            mIntent = Intent(this@IndexActivity, CameraActivity::class.java)
            startActivity(mIntent)
        }
    }
}
