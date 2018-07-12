package com.example.surfaceview.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.surfaceview.R
import java.lang.Exception

class DrawSurfaceView
(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        mThread.isRun = false
        mThread.interrupt()
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        mThread.isRun = true
        mThread.start()
    }

    private var mThread: MyThread


    init {
        val surfaceHolder = holder
        surfaceHolder.addCallback(this)
        var bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
        mThread = MyThread(surfaceHolder, bitmap)
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT)//顶层设置透明
        setZOrderOnTop(true)
    }

    fun startDraw() {
        mThread.isRun = true
    }

    fun stopDraw() {
        mThread.isRun = false
    }

    private class MyThread(holder: SurfaceHolder, bitmap: Bitmap) : Thread() {
        var isRun = false
        val mHolder: SurfaceHolder = holder
        lateinit var canvas: Canvas
        val mBitmap: Bitmap = bitmap
        val matrix = Matrix()
        override fun run() {
            var rotate = 0.0F
            val p = Paint()
            while (isRun) {
                try {
                    canvas = mHolder.lockCanvas(Rect(0, 0, 300, 300))
                    //设置旋转角度
                    rotate += 48.0F
                    matrix.postRotate(rotate % 360.0F, mBitmap.width / 2.0F, mBitmap.height / 2.0F)
                    //设置左边距和上边距
                    matrix.postTranslate(0.0F, 0.0F)
                    canvas.drawBitmap(mBitmap, matrix, p)

                    Thread.sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    mHolder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}