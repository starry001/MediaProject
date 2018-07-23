package com.example.surfaceview.opengl

import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class My01GLRenderer : GLSurfaceView.Renderer {
    private var mSquare: Square? = null
    private var mTriangle: Triangle? = null


    //mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)


    var angle: Float = 0.toFloat()

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        Log.e(TAG, "onSurfaceCreated")
        // 设置清屏颜色
        GLES31.glClearColor(255.0f, 255.0f, 255.0f, 1.0f)//OpenGL支持两种颜色模式：一种是RGBA

        mTriangle = Triangle() //定义一个三角形
        mSquare = Square() //定义一个正方形
    }

    override fun onDrawFrame(unused: GL10) {
        Log.e(TAG, "onDrawFrame")
        val scratch = FloatArray(16)

        //调用glClear(GL10.GL_COLOR_BUFFER_BIT)方法清除屏幕颜色,执行这个方法之后
        //屏幕就会渲染之前通过glClearColor设置的清屏颜色.
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                0f, 0f, -3f,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f)

        // 计算投影及视图变化
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        // 绘制正方形
        mSquare!!.draw(mMVPMatrix)

        // 创建一个旋转三角形

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);

        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)

        // 绘制三角形
        mTriangle!!.draw(scratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        Log.e(TAG, "onSurfaceChanged")
        // 设置窗口大小 调整视点的几何变化,如屏幕旋转
        GLES31.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height

        // 投影矩阵应用在坐标系中,当onDrawFrame方向调用时
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    companion object {
        private const val TAG = "MyGLRender"

        fun checkGLError(glOperation: String) {
            val error = GLES31.glGetError()
            if (error != GLES31.GL_NO_ERROR) {
                throw RuntimeException("$glOperation : glError = $error")
            }
        }

        fun loadShader(type: Int, shaderCode: String): Int {
            //create the vritex shader type
            //or a fragmentshader type
            val shader = GLES31.glCreateShader(type)

            //add the source to the shader and compile it
            if (shader != 0) {
                GLES31.glShaderSource(shader, shaderCode)
                GLES31.glCompileShader(shader)
            }

            return shader
        }
    }
}