package com.example.surfaceview.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.text.TextUtils
import java.lang.Exception

object Utils {
    //是否强支持camera2
    fun isSupportCamera2(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return false
        }
        var isFullSupport = true
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = cameraManager.cameraIdList
            if (cameraIdList.isEmpty()) {
                return false
            }
            for (id in cameraIdList) {
                if (TextUtils.isEmpty(id)) {
                    isFullSupport = false
                    break
                }
                val characteristics = cameraManager.getCameraCharacteristics(id)
                //来根据返回值来获取支持的级别
                val supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                //所有设备都会支持，也就是和过时的Camera API支持的特性是一致的
                if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    isFullSupport = false
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return isFullSupport
    }
}