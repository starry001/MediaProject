package com.example.surfaceview.ui

import android.os.Environment
import android.widget.MediaController
import android.widget.VideoView
import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import com.example.surfaceview.util.Constant
import kotlinx.android.synthetic.main.activity_media_player.*
import java.io.File

class MediaPlayerActivity : BaseActivity() {

    private val videoPath: File by lazy {
        File(Environment.getExternalStorageDirectory(), Constant.MP4_NAME)
    }

    private lateinit var systemVideoView: VideoView


    override fun layoutId(): Int = R.layout.activity_media_player

    override fun initListener() {
        //system meida player
        systemVideoView = videoView
        val mediaControl = MediaController(this)
        mediaControl.show()
        systemVideoView.setMediaController(mediaControl)
        systemVideoView.setVideoPath(videoPath.toString())

        //costom media player
        mPlayerView.setVideoFilePath(videoPath.toString())
    }

    override fun initData() {

    }

    override fun onPause() {
        mPlayerView.pause()
        systemVideoView.pause()
        super.onPause()
    }
}
