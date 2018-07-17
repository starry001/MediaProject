package com.example.surfaceview.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.MediaController
import com.example.surfaceview.`interface`.IPlayerCallBack
import com.example.surfaceview.model.VideoPlayer

class PlayerView : SurfaceView, MediaController.MediaPlayerControl, IPlayerCallBack {

    private lateinit var mMediaController: MediaController
    private lateinit var mVideoPlayer: VideoPlayer

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init()
    }

    private fun init() {
        mVideoPlayer = VideoPlayer(holder.surface)
        mVideoPlayer.setCallBack(this)
        mMediaController = MediaController(context)
        mMediaController.setMediaPlayer(this)
    }

    override fun isPlaying(): Boolean = mVideoPlayer.isPlaying()

    override fun canSeekForward(): Boolean = true

    override fun getDuration(): Int = 0

    override fun pause() {
        mVideoPlayer.stop()
    }

    override fun getBufferPercentage(): Int = 0

    override fun seekTo(pos: Int) {

    }

    override fun getCurrentPosition(): Int = 0

    override fun canSeekBackward(): Boolean = true

    override fun start() {
        mVideoPlayer.play()
    }

    override fun getAudioSessionId(): Int = 1

    override fun canPause(): Boolean = true

    override fun videoAspect(width: Int, height: Int, time: Float) {
        Log.e("PlayerView", "width = $width,height = $height,time = $time")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachMediaController()
    }

    private fun attachMediaController() {
        val anchorView: View = if (parent is View) {
            parent as View
        } else {
            this
        }
        mMediaController.setAnchorView(anchorView)
        mMediaController.isEnabled = true
    }

    fun getMediaController(): MediaController = mMediaController

    fun setVideoFilePath(videoFilePath: String) {
        mVideoPlayer.setFilePath(videoFilePath)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.getAction() == MotionEvent.ACTION_DOWN) {
            if (!mMediaController.isShowing) {
                mMediaController.show()
            } else {
                mMediaController.hide()
            }
        }
        return super.onTouchEvent(event)
    }
}