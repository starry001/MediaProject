package com.example.surfaceview.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.surfaceview.R
import com.example.surfaceview.util.WindEar
import com.example.surfaceview.util.showToast
import kotlinx.android.synthetic.main.activity_audio.*

class AudioActivity : AppCompatActivity() ,WindEar.OnState{
    private val tag = AudioActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

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

    override fun onStateChanged(currentState: WindEar.WindState?) {
//        Log.e(tag,"currentState : $currentState")
    }
}
