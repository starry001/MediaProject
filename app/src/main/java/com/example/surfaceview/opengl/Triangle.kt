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

        // number of coodinates pre vertex in this array
        private const val COORDS_PRE_VERTEX = 3

        private val triangleCoords = floatArrayOf(
                0.0F, 0.622008459F, 0.0F, // top
                -0.5F, -0.311004243F, 0.0F,// bottom left
                0.5F, -0.311004243F, 0.0F   // top right
        )
        private val vertexCount = triangleCoords.size / COORDS_PRE_VERTEX
        //4 byte pre vertex
        private const val vertexStride = COORDS_PRE_VERTEX * 4
        private val color = floatArrayOf(
                0.63671875F, 0.76953125F, 0.22265625F, 0.0F
        )
    }

    private var vertexBuffer: FloatBuffer
    private var mProgram = 0
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mMVPMatrixHandle = 0

    init {
        // init vertext byte buffer for  shape coordinates
        val bb = ByteBuffer.allocateDirect(
                triangleCoords.size * 4 // of coordinate values * 4 byte pre float
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
        // add te vertex shader to program
        GLES31.glAttachShader(mProgram, vertexShader)
        // add the fragment shader to program
        GLES31.glAttachShader(mProgram, fragmentShader)

        //create OpenGL program executable
        GLES31.glLinkProgram(mProgram)
    }

    fun draw(mvpMatrix: FloatArray) {
        //add program to the OpenGL environment
        GLES31.glUseProgram(mProgram)
        //get handle to vertex shader's vPosition mumber
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition")
        //enable the triangle coordinate data
        GLES31.glEnableVertexAttribArray(mPositionHandle)
        //prepare the triangle coordinate data
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PRE_VERTEX,
                GLES31.GL_FLOAT, false, vertexStride, vertexBuffer)

        //get handle to fragment shader's vColor mumber
        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor")
        //set color for draw the triangle
        GLES31.glUniform4fv(mColorHandle, 1, color, 0)

        //get handle to shape's transformation matrix
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