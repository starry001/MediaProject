package com.example.surfaceview.opengl

import android.opengl.GLES31
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle {

    companion object {
        private const val vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        " gl_Position = uMVPMatrix * vPosition;" +
                        "}"
        private const val fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "void main() {" +
                        " gl_FragColor = vColor;" +
                        "}"

        // 设置每个顶点的坐标数
        private const val COORDS_PRE_VERTEX = 3

        // 默认按逆时针方向顺序绘制
        private val triangleCoords = floatArrayOf(
                0.0F, 0.5F, 0.0F, // top
                -0.5F, -0.5F, 0.0F,// bottom left
                0.5F, -0.5F, 0.0F   // top right
        )
        private val vertexCount = triangleCoords.size / COORDS_PRE_VERTEX
        // 4 byte pre vertex
        private const val vertexStride = COORDS_PRE_VERTEX * 4
        // 设置图形的RGB值和透明度
        private val color = floatArrayOf(
                0.63671875F, 0.76953125F, 0.22265625F, 0.0F
        )
    }

    private var vertexBuffer: FloatBuffer //顶点坐标数据缓冲
    private var mProgram = 0 //自定义渲染管线程序id
    private var mPositionHandle = 0 //总变换矩阵引用id
    private var mColorHandle = 0 //顶点位置属性引用id
    private var mMVPMatrixHandle = 0 //总变换矩阵引用id

    init {
        // init vertext byte buffer for  shape coordinates
        val bb = ByteBuffer.allocateDirect(
                triangleCoords.size * 4 //一个float 4个字节
        )
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)


        // prepare shaders and OpenGL program
        val vertexShader = MyGLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode)

        //create an empty program
        mProgram = GLES31.glCreateProgram()

        // 向程序中加入顶点着色器
        GLES31.glAttachShader(mProgram, vertexShader)
        // 向程序中加入片元着色器
        GLES31.glAttachShader(mProgram, fragmentShader)
        // 链接程序
        GLES31.glLinkProgram(mProgram)
    }

    fun draw(mvpMatrix: FloatArray) {
        //add program to the OpenGL environment
        GLES31.glUseProgram(mProgram)
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition")
        // 允许顶点位置数据数组
        GLES31.glEnableVertexAttribArray(mPositionHandle)
        // 为画笔指定顶点位置数据
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PRE_VERTEX,
                GLES31.GL_FLOAT, false, vertexStride, vertexBuffer)
        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor")
        //set color for draw the triangle
        GLES31.glUniform4fv(mColorHandle, 1, color, 0)
        mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix")
        MyGLRenderer.checkGLError("glGetUniformLocation")
        //apply the projection and view transformation
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        MyGLRenderer.checkGLError("glUniformMatrix4fv")
        //draw the triangle
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount)

        //disbale vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle)
    }
}