package com.example.surfaceview.`interface`

import android.hardware.Camera

@Suppress("DEPRECATION")
interface ICamera {
    fun takePhoto(camera: Camera, pictureCallback: Camera.PictureCallback)

    fun reserveCamera(camera: Camera, cameraPosition: Int)

    fun startPreview(camera: Camera)

    fun stopPreview(camera: Camera)


}