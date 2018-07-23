package com.example.surfaceview.opengl

import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class My02GLRenderer : GLSurfaceView.Renderer {
    private val tag = "My02GLRenderer"
    private var coordinate: Coordinate = Coordinate()
    private var mAngle = 45f

    override fun onDrawFrame(gl: GL10?) {
        Log.e(tag, ">> onDrawFrame mAngle $mAngle")

        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)
        gl!!.run {
            glLoadIdentity()
//            glTranslatef(0f, 0.3f, -0.2f)
            glRotatef(mAngle,1f,1f,1f)
        }
        mAngle++
        coordinate.draw(gl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        var h = height
        if (height == 0) {
            h = 1
        }
        val aspect = width.toFloat() / h
        gl!!.run {
            glViewport(0, 0, width, h)
            //OpenGL支持两种类型的投影变换，即透视投影和正投影。投影也是使用矩阵来实现的。
            //如果需要操作投影矩阵，需要以GL_PROJECTION为参数调用glMatrixMode函数
            glMatrixMode(GL10.GL_PROJECTION)
            //在进行变换前把当前矩阵设置为单位矩阵
            glLoadIdentity()
            GLU.gluPerspective(gl, 45f, aspect, 0.1f, 100f)
            //设置3D模型的位移，旋转等属性。由于模型和视图的变换都通过矩阵运算来实现，
            //在进行变换前，应先设置当前操作的矩阵为“模型视图矩阵”。
            //设置的方法是以GL_MODELVIEW为参数调用glMatrixMode函数
            glMatrixMode(GL10.GL_MODELVIEW)
            glLoadIdentity()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl!!.run {
            glClearColor(255f, 255f, 255f, 1f)
            glClearDepthf(1f)
            glEnable(GL10.GL_DEPTH_TEST)
            glDepthFunc(GL10.GL_LEQUAL)
            glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST)
            glShadeModel(GL10.GL_SMOOTH)
            glDisable(GL10.GL_DITHER)
        }
    }
}