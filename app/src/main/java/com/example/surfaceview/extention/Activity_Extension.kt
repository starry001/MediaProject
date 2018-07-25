package com.example.surfaceview.extention

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.UiThread
import android.view.View
import android.widget.Toast

@UiThread
fun Activity.showToast(msg: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, duration).show()
}

fun <T : View> Activity.findView(id: Int) = findViewById(id) as T

inline fun <reified T : Activity> Activity.jump(bundle: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}
