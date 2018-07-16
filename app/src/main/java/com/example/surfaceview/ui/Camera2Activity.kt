package com.example.surfaceview.ui

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.SparseArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.example.surfaceview.R
import com.example.surfaceview.base.BaseActivity
import com.example.surfaceview.util.ImageUtils
import com.example.surfaceview.util.Utils
import com.example.surfaceview.util.logger
import kotlinx.android.synthetic.main.activity_camera2.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class Camera2Activity : BaseActivity() {
    private val STATE_PREVIEW = 0
    private val STATE_WAITING_LOCK = 1
    private val STATE_WAITING_PRE_CAPTURE = 2
    private val STATE_WAITING_NON_PRE_CAPTURE = 3
    private val STATE_PICTURE_TAKEN = 4

    private var mPreviewState = STATE_PREVIEW

    private lateinit var mCameraDevice: CameraDevice
    //摄像头ID（通常0代表后置摄像头，1代表前置摄像头
    private lateinit var mCameraId: String
    private lateinit var mCameraManager: CameraManager

    private lateinit var mBackgroundThread: HandlerThread
    private lateinit var mBackgroundHandler: Handler
    private lateinit var mImageReader: ImageReader

    private lateinit var mPreviewRequestBuilder: CaptureRequest.Builder
    private var mCaptureSession: CameraCaptureSession? = null
    private lateinit var mPreviewRequest: CaptureRequest
    private var mFlashSupported = false//是否支持闪光灯
    private lateinit var mFile: File
    private var mSensorOrientation = 0

    private var ORIENTATIONS: SparseArray<Int> = SparseArray(4)

    override fun layoutId(): Int = R.layout.activity_camera2

    override fun initListener() {
        texture_View.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                openCamera()
            }
        }
        take_photo.setOnClickListener {
            lockFocus()
        }
    }

    override fun initData() {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)

        startBackgroundThread()
        initCameraManager()
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread.start()
        mBackgroundHandler = Handler(mBackgroundThread.looper)
    }

    fun getPreviewSize(): Size {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private fun openCamera() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        mImageReader = ImageReader.newInstance(getPreviewSize().width, getPreviewSize().height, ImageFormat.JPEG, 2)
        mImageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(p0: ImageReader?) {
                mFile = ImageUtils.getOutputMediaFile(ImageUtils.MEDIA_TYPE_IMAGE)
                mBackgroundHandler.post(ImageSaver(p0?.acquireNextImage(), mFile))
            }
        }, mBackgroundHandler)

        mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
    }

    private class ImageSaver(image: Image?, file: File) : Runnable {
        private var mImage = image
        private val imageFile = file

        override fun run() {
            val buffer = mImage!!.getPlanes()[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val output = FileOutputStream(imageFile)
            try {
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    mImage?.close()
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun closeCamera() {
        try {
            mCaptureSession?.abortCaptures()
            mCaptureSession?.close()
            mImageReader.close()
            mCameraDevice.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopBackgroundThread() {
        mBackgroundThread.quitSafely()
        try {
            mBackgroundThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        closeCamera()
        stopBackgroundThread()
        super.onStop()
    }

    private fun initCameraManager() {
        mCameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val ids = mCameraManager.cameraIdList
        logger("${Utils.isSupportCamera2(this)}")
        for (id in ids) {
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(id)
            //获取摄像头
            val orientation = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            when (orientation) {
            //前置摄像头
                CameraCharacteristics.LENS_FACING_FRONT -> {

                }
            //后置摄像头
                CameraCharacteristics.LENS_FACING_BACK -> {
                    mCameraId = id
                }
            }
            val available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            mFlashSupported = available ?: false

            mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        }
    }

    private var mStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(p0: CameraDevice?) {
            mCameraDevice = p0!!
            createCameraPreviewSession()
        }

        override fun onDisconnected(p0: CameraDevice?) {
            p0?.close()
        }

        override fun onError(p0: CameraDevice?, p1: Int) {
            p0?.close()
        }
    }

    private fun createCameraPreviewSession() {
        val surfaceTexture = texture_View.surfaceTexture
        surfaceTexture.setDefaultBufferSize(getPreviewSize().width, getPreviewSize().height)

        val surface = Surface(surfaceTexture)
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewRequestBuilder.addTarget(surface)
        mPreviewRequest = mPreviewRequestBuilder.build()

        mCameraDevice.createCaptureSession(arrayListOf(surface, mImageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(p0: CameraCaptureSession?) {

                    }

                    override fun onConfigured(p0: CameraCaptureSession?) {
                        mCaptureSession = p0
                        // 设置预览时连续捕获图像数据 持续的进行预览
                        mCaptureSession?.setRepeatingRequest(mPreviewRequest,
                                captureCallback, mBackgroundHandler)
                        this@Camera2Activity.runOnUiThread {
                            take_photo.visibility = View.VISIBLE
                        }
                    }
                }, null)
    }

    private var captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?,
                                        result: TotalCaptureResult?) {
            processCaptureResult(result!!)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            processCaptureResult(partialResult!!)
        }
    }

    private fun processCaptureResult(result: CaptureResult) {
        when (mPreviewState) {
            STATE_PREVIEW -> {
            }
            STATE_WAITING_LOCK -> {
                val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                if (afState == null) {
                    captureStillPicture()
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mPreviewState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    } else {
                        runPreCaptureSequence()
                    }
                }
            }
            STATE_WAITING_PRE_CAPTURE -> {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    mPreviewState = STATE_WAITING_NON_PRE_CAPTURE
                }
            }
            STATE_WAITING_NON_PRE_CAPTURE -> {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    mPreviewState = STATE_PICTURE_TAKEN
                    captureStillPicture()
                }
            }
            STATE_PICTURE_TAKEN -> {
            }
        }
    }

    private fun runPreCaptureSequence() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            mPreviewState = STATE_WAITING_PRE_CAPTURE
            mCaptureSession?.capture(mPreviewRequestBuilder.build(), captureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun lockFocus() {
        try {
            //相机对焦
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START)
            mPreviewState = STATE_WAITING_LOCK
            //发送对焦请求
            mCaptureSession?.capture(mPreviewRequestBuilder.build(), captureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace();
        }
    }

    private fun getOrientation(rotation: Int): Int {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
    }

    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger 重设自动对焦模式
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            // 设置自动曝光模式
            mPreviewState = STATE_PREVIEW
            mCaptureSession?.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler)
            // 打开连续取景模式
            mCaptureSession?.setRepeatingRequest(mPreviewRequest, captureCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //捕捉静止的画面
    private fun captureStillPicture() {
        //构建用来拍照的CaptureRequest
        val captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(mImageReader.surface)

        // 设置自动对焦模式
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        // Orientation 根据设备方向计算设置照片的方向
        val rotation = windowManager.defaultDisplay.rotation
        //设置方向
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

        val captureCallback = object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureCompleted(session: CameraCaptureSession,
                                            request: CaptureRequest,
                                            result: TotalCaptureResult) {
                logger("onCaptureCompleted: ")
                unlockFocus()
            }
        }
        // 停止连续取景
        mCaptureSession?.stopRepeating()
        // 捕获静态图像
        mCaptureSession?.capture(captureBuilder.build(), captureCallback, null)
    }
}
