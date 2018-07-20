package com.example.surfaceview.opengl

import android.opengl.GLES31
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Square {

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
        // 每个顶点需要的坐标
        private const val COORDS_PRE_VERTEX = 3

        private val squareCoords = floatArrayOf(
                -0.5F, 0.5F, 0.0F, // top left
                -0.5F, -0.5F, 0.0F,// bottom left
                0.5F, -0.5F, 0.0F, // bottom right
                0.5F, 0.5F, 0.0F   // top right
        )
        // order to draw vertices
        private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
        // 每个顶点坐标的byte大小
        private const val vertexStride = COORDS_PRE_VERTEX * 4
        private val color = floatArrayOf(
                0.5F, 0.5F, 0.5F, 1.0F
        )
    }

    private var vertexBuffer: FloatBuffer
    private var drawListBuffer: ShortBuffer
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mMVPMatrixHandle = 0

    // set up drawing object data for use in an OpenGL ES context
    init {
        // init vertext byte buffer for  shape coordinates
        val bb = ByteBuffer.allocateDirect(
                squareCoords.size * 4 // of coordinate values * 4 byte pre float
        )
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(squareCoords)
        vertexBuffer.position(0)

        //init byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(
                drawOrder.size * 2
        )
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        // prepare shaders and OpenGL program
        val vertexShader = MyGLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode)

        //create an empty program
        mProgram = GLES31.glCreateProgram()

        GLES31.glAttachShader(mProgram, vertexShader)
        GLES31.glAttachShader(mProgram, fragmentShader)
        GLES31.glLinkProgram(mProgram)
    }

    fun draw(mvpMatrix: FloatArray) {
        //add program to the OpenGL environment
        GLES31.glUseProgram(mProgram)
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition")
        //enable the triangle coordinate data
        GLES31.glEnableVertexAttribArray(mPositionHandle)
        //prepare the triangle coordinate data
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PRE_VERTEX,
                GLES31.GL_FLOAT, false, vertexStride, vertexBuffer)
        //set color for draw the triangle
        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor")
        GLES31.glUniform4fv(mColorHandle, 1, color, 0)

        mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix")
        MyGLRenderer.checkGLError("glGetUniformLocation")

        //apply the projection and view transformation
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        MyGLRenderer.checkGLError("glUniformMatrix4fv")

        //draw the square
        GLES31.glDrawElements(GLES31.GL_TRIANGLES, drawOrder.size,GLES31.GL_UNSIGNED_SHORT,drawListBuffer)

        //disbale vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle)
    }
}