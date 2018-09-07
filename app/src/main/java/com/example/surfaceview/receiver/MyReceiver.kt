package com.example.surfaceview.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.example.surfaceview.extention.logger

class MyReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        logger("MyReceiver", "cur thread name is " + Thread.currentThread().name)

        val s = intent?.getStringExtra("main")
        if (!TextUtils.isEmpty(s)) {
            logger("MyReceiver", s)
        } else {
            logger("MyReceiver", "null")
        }

        val bundle = Bundle()
        bundle.putString("test", "value from MyReceiver")
        setResultExtras(bundle)
    }
}