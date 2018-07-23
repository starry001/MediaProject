package com.example.surfaceview.opengl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class Coordinate {
    private var vertexsBuffer: FloatBuffer
    private var colorsBuffer: FloatBuffer

    private val vertex = floatArrayOf(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 0f,
            0f, 0f, 1f
    )

    private val colors = floatArrayOf(
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f
    )

    init {
        val vbb = ByteBuffer.allocateDirect(vertex.size * 4)
        vbb.order(ByteOrder.nativeOrder())
        vertexsBuffer = vbb.asFloatBuffer()
        vertexsBuffer.put(vertex)
        vertexsBuffer.position(0)

        val cbb = ByteBuffer.allocateDirect(colors.size * 4)
        cbb.order(ByteOrder.nativeOrder())
        colorsBuffer = cbb.asFloatBuffer()
        colorsBuffer.put(colors)
        colorsBuffer.position(0)
    }

    fun draw(gl: GL10) {
        gl.run {
            // 指定需要启用定点数组 默认关闭
            glEnableClientState(GL10.GL_VERTEX_ARRAY)
            glEnableClientState(GL10.GL_COLOR_ARRAY)
            // 说明启用数组的类型和字节缓冲，类型为GL_FLOAT
            glVertexPointer(3, GL10.GL_FLOAT, 0, vertexsBuffer)
            glColorPointer(4, GL10.GL_FLOAT, 0, colorsBuffer)
            glLineWidth(9f)
            glDrawArrays(GL10.GL_LINES, 0, vertex.size / 3)
            // 不再需要时，关闭顶点数组
            glDisableClientState(GL10.GL_VERTEX_ARRAY)
        }
    }
}