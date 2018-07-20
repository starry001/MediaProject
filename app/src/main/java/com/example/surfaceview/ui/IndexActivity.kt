package com.example.surfaceview.ui

import android.content.Intent
import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import kotlinx.android.synthetic.main.activity_index.*

class IndexActivity : BaseActivity() {
    private lateinit var mIntent: Intent

    override fun layoutId(): Int = R.layout.activity_index

    override fun initListener() {
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

        camera2.setOnClickListener {
            mIntent = Intent(this@IndexActivity, Camera2Activity::class.java)
            startActivity(mIntent)
        }

        mediaPlayer.setOnClickListener {
            mIntent = Intent(this@IndexActivity, MediaPlayerActivity::class.java)
            startActivity(mIntent)
        }

        media_split.setOnClickListener {
            mIntent = Intent(this@IndexActivity, MediaApplicationActivity::class.java)
            startActivity(mIntent)
        }

        opengl.setOnClickListener {
            mIntent = Intent(this@IndexActivity, OpenGLESActivity::class.java)
            startActivity(mIntent)
        }
    }

    override fun initData() {

    }
}
