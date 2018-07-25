package com.example.surfaceview.ui

import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import com.example.surfaceview.util.JNIUtils
import com.example.surfaceview.extention.jump
import com.example.surfaceview.extention.logger
import kotlinx.android.synthetic.main.activity_index.*

class IndexActivity : BaseActivity() {

    override fun layoutId(): Int = R.layout.activity_index

    override fun initListener() {
        surface.setOnClickListener {
            jump<SurfaceActivity>()
        }

        audio.setOnClickListener {
            jump<AudioActivity>()
        }

        camera.setOnClickListener {
            jump<CameraActivity>()
        }

        camera2.setOnClickListener {
            jump<Camera2Activity>()
        }

        mediaPlayer.setOnClickListener {
            jump<MediaPlayerActivity>()
        }

        media_split.setOnClickListener {
            jump<MediaApplicationActivity>()
        }

        opengl.setOnClickListener {
            jump<OpenGLMainActivity>()
        }
    }

    override fun initData() {
        val str = JNIUtils.stringFromJNI()
        logger(str)
    }
}
