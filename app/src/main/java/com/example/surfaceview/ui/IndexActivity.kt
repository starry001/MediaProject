package com.example.surfaceview.ui

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import com.example.surfaceview.extention.jump
import com.example.surfaceview.extention.logger
import com.example.surfaceview.receiver.MsgReceiver
import com.example.surfaceview.util.JNIUtils
import kotlinx.android.synthetic.main.activity_index.*


class IndexActivity : BaseActivity() {

    private var handler: Handler? = null
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

        test_receiver.setOnClickListener {
            val receiver = MsgReceiver()
            val intent = Intent("com.example.test")
            intent.putExtra("main", "this is from IndexActivity");
            sendOrderedBroadcast(intent, null, receiver,
                    handler, -1, null, null)
        }
    }

    override fun initData() {
        val str = JNIUtils.stringFromJNI()
        logger(str)
        val handlerThread = HandlerThread("test")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }
}
