package com.example.surfaceview.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.surfaceview.extention.logger

class MsgReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        logger("MsgReceiver", "cur thread name is " + Thread.currentThread().name)
        val bundle = getResultExtras(true)
        val str = bundle.getString("test")
        logger("MsgReceiver", "str = $str")
    }
}