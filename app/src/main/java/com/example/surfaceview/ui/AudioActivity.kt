package com.example.surfaceview.ui

import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import com.example.surfaceview.util.WindEar
import com.example.surfaceview.extention.showToast
import kotlinx.android.synthetic.main.activity_audio.*



class AudioActivity : BaseActivity() ,WindEar.OnState{

    override fun layoutId(): Int  = R.layout.activity_audio

    override fun initListener() {
        WindEar.getInstance().setOnStateListener(this)
        WindEar.init()

        record.setOnClickListener {
            showToast("start recording!")
            WindEar.getInstance().startRecord(true)
        }

        stop_record.setOnClickListener {
            WindEar.getInstance().stopRecord()
        }

        play.setOnClickListener {
            WindEar.getInstance().startPlayPCM()
            WindEar.getInstance().startPlayWav()
        }

        stop_play.setOnClickListener {
            WindEar.getInstance().stopPlay()
        }
    }

    override fun initData() {

    }

    override fun onStateChanged(currentState: WindEar.WindState?) {

    }
}
