package com.example.surfaceview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.Exception

class RectSurfaceView : SurfaceView, SurfaceHolder.Callback {
    private lateinit var mThread: DrawThread

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        val surfaceHolder = holder
        surfaceHolder.addCallback(this)
        mThread = DrawThread(surfaceHolder)
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        mThread.isRun = false
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        mThread.isRun = true
        mThread.start()
    }


    fun startDraw() {
        mThread.isRun = true
    }

    fun stopDraw() {
        mThread.isRun = false
    }


    class DrawThread(holder: SurfaceHolder) : Thread() {

        private val mHolder: SurfaceHolder = holder
        var isRun = false

        override fun run() {

            var count = 0

            val mPaint = Paint()
            mPaint.color = Color.BLACK
            mPaint.textSize = 20F
            val rect = Rect(100, 50, 300, 250)
            while (isRun) {
                var canvas: Canvas?
                synchronized(mHolder) {
                    canvas = mHolder.lockCanvas()
                    canvas?.drawColor(Color.WHITE)

                    canvas?.drawRect(rect, mPaint)
                    canvas?.drawText("""这是第${count++}秒""", 150.0F, 310.0F, mPaint)

                    try {
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        mHolder.unlockCanvasAndPost(canvas!!)
                    }
                }
            }
        }
    }
}