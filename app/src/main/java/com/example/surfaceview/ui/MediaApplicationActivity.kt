package com.example.surfaceview.ui

import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import com.example.surfaceview.util.Constant
import com.example.surfaceview.util.MediaUtils
import kotlinx.android.synthetic.main.activity_media_application.*
import kotlinx.coroutines.experimental.launch

class MediaApplicationActivity : BaseActivity() {


    override fun layoutId(): Int = R.layout.activity_media_application

    override fun initListener() {
        split_all.setOnClickListener {
            launch {
                MediaUtils.getInstance().exactorMedia()
            }
        }

        split_audio.setOnClickListener {
            launch {
                MediaUtils.getInstance().muxerAudio(Constant.SDCARD_PATH + "/" + Constant.MP4_NAME)
            }
        }

        split_video.setOnClickListener {
            launch {
                MediaUtils.getInstance().muxerMedia(Constant.SDCARD_PATH + "/" + Constant.MP4_NAME)
            }
        }

        combine.setOnClickListener {
            launch {
                MediaUtils.getInstance().combineVideo()
            }
        }
    }

    override fun initData() {

    }
}
