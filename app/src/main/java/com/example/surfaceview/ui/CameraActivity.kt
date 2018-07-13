package com.example.surfaceview.ui

import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.renderscript.RenderScript
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import android.view.SurfaceHolder
import com.example.surfaceview.R
import com.example.surfaceview.util.ImageUtils
import com.example.surfaceview.util.logger
import kotlinx.android.synthetic.main.activity_camera.*

@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback {
    private val width = 1280
    private val height = 720
    private val imageFormat = ImageFormat.YV12

    private lateinit var camera: Camera
    private lateinit var holder: SurfaceHolder
    private lateinit var cameraParam: Camera.Parameters

    private var cameraPosition = 0

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        releaseCamera()
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        try {
            openCamera(p0) // open camera
        } catch (e: Exception) {
            logger("openCamera", e.message ?: "")
        }
    }

    private fun openCamera(holder: SurfaceHolder?) {
        RenderScript.create(this)
        cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)

        setUpParams(camera, cameraPosition)
    }

    private fun setUpParams(mCamera: Camera, pos: Int) {
        cameraParam = mCamera.parameters
        setCameraDisplayOrientation(pos, mCamera)
        cameraParam.run {
            jpegQuality = 80
            set("rotation", 90)
            focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            sceneMode = Camera.Parameters.SCENE_MODE_AUTO
            val supportedPreviewSizes = cameraParam.supportedPreviewSizes
            val supportedPictureSizes = cameraParam.supportedPictureSizes

            val previewSize = ImageUtils.getCurrentScreenSize(this@CameraActivity, supportedPreviewSizes, 1)
            val pictureSize = ImageUtils.getCurrentScreenSize(this@CameraActivity, supportedPictureSizes, 1)

            setPreviewSize(previewSize.width, previewSize.height)
            setPictureSize(pictureSize.width, pictureSize.height)
            pictureFormat = ImageFormat.JPEG
        }
        mCamera.run {
            parameters = cameraParam
            setPreviewDisplay(holder)
            setPreviewCallback(this@CameraActivity)
            startPreview()
        }
    }

    private fun setCameraDisplayOrientation(cameraId: Int, mCamera: Camera) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)

        val degrees = getRotateDegree()

        var displayDegree: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360
            displayDegree = (360 - displayDegree) % 360  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360
        }
        mCamera.setDisplayOrientation(displayDegree)
    }

    fun getRotateDegree(): Int {
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        return degrees
    }

    private fun releaseCamera() {
        camera.run {
            try {
                setPreviewCallback(null)
                stopPreview()
                release()
            } catch (e: Exception) {
                logger("releaseCamera", e.message ?: "")
            }
        }
        holder.surface.release()
    }

    override fun onPreviewFrame(p0: ByteArray?, p1: android.hardware.Camera?) {
        camera.addCallbackBuffer(p0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initSurfaceHolder()

        //拍照
        take_photo.setOnClickListener {
            try {
                camera.takePicture(null, null, object : Camera.PictureCallback {
                    override fun onPictureTaken(p0: ByteArray?, p1: Camera?) {
                        ImageUtils.saveImageData(p0)
                    }
                })
            } catch (e: Exception) {
                logger("takePicture", e.message ?: "null")
            }
        }

        //反转摄像头
        reserve_camera.setOnClickListener {
            camera.run {
                stopPreview()
                release()
            }
            if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT
            } else {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK
            }
            setUpParams(camera, cameraPosition)
        }

        //重拍
        retake.setOnClickListener {
            camera.startPreview()
        }
    }

    private fun initSurfaceHolder() {
        holder = camera_surface.holder
        holder.run {
            addCallback(this@CameraActivity)
            setFixedSize(width, height)
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
    }
}
