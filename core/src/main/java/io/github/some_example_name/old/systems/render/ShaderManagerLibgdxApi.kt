package io.github.some_example_name.old.systems.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL31
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.BufferUtils
import java.nio.ByteBuffer
import java.nio.IntBuffer

interface ShaderManager {
    fun create()
    fun render(currentRead: ByteBuffer, cameraProjection: Matrix4)
    fun dispose()
}

class ShaderManagerLibgdxApi : ShaderManager {
    // Три SSBO IDs для тройного буфера на GPU (ротация для избежания stalls)
    private val ssboIds = IntArray(3)
    private var currentSsboIndex = 0  // Для ротации SSBO

    private lateinit var shader: ShaderProgram
    private lateinit var mesh: Mesh

    override fun create() {
        // Создание трёх SSBO с использованием Gdx.gl31
        val ssboBuffer: IntBuffer = BufferUtils.newIntBuffer(3)
        Gdx.gl31.glGenBuffers(3, ssboBuffer)
        for (i in 0 until 3) {
            ssboIds[i] = ssboBuffer.get(i)
            Gdx.gl31.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, ssboIds[i])
            Gdx.gl31.glBufferData(
                GL31.GL_SHADER_STORAGE_BUFFER,
                TripleBufferManager.BUFFER_SIZE_BYTES,
                null,
                GL20.GL_DYNAMIC_DRAW
            )
            // Нет необходимости в glBindBufferBase здесь, так как binding динамический в render
        }
        Gdx.gl31.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, 0)

        val vertexShader = Gdx.files.internal("shaders/debug/circle.vert").readString()
        val fragmentShader = Gdx.files.internal("shaders/debug/circle.frag").readString()
        shader = ShaderProgram(vertexShader, fragmentShader)
        println("lol kek")
        if (!shader.isCompiled) {
            throw RuntimeException("Shader compilation failed: ${shader.log}")
        }

        val vertices = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        val attributes = VertexAttributes(
            VertexAttribute(
                VertexAttributes.Usage.Position,
                2,
                ShaderProgram.POSITION_ATTRIBUTE
            )
        )
        mesh = Mesh(false, 4, 0, attributes).apply { setVertices(vertices) }
    }

    override fun render(
        currentRead: ByteBuffer,
        cameraProjection: Matrix4
    ) {
        // Ротация SSBO: Выбираем следующий ID
        val ssboId = ssboIds[currentSsboIndex]
        currentSsboIndex = (currentSsboIndex + 1) % 3

        // Аплоад данных в текущий SSBO
        Gdx.gl31.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, ssboId)
//        val dataSize = currentRead.remaining()
        val dataSize = currentRead.remaining()
        val numInstances = dataSize / 16  // 16 байт на частицу
        if (numInstances == 0) return  // Нет данных — skip
        Gdx.gl31.glBufferSubData(GL31.GL_SHADER_STORAGE_BUFFER, 0, dataSize, currentRead)
        Gdx.gl31.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, 0)

        // Bind SSBO к binding point 0
        Gdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 0, ssboId)

        // Рендер (адаптируйте под ваш mesh и draw call, здесь пример для instanced draw)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shader.bind()
        shader.setUniformMatrix("u_projTrans", cameraProjection)  // Установите uniform если нужно
        mesh.bind(shader)
        Gdx.gl31.glDrawArraysInstanced(GL20.GL_TRIANGLE_STRIP, 0, 4, TripleBufferManager.PARTICLE_MAX_AMOUNT)  // Пример для quad mesh
        mesh.unbind(shader)

        // Unbind SSBO
        Gdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 0, 0)
    }

    override fun dispose() {
        shader.dispose()
        val ssboBuffer: IntBuffer = BufferUtils.newIntBuffer(3)
        for (id in ssboIds) {
            ssboBuffer.put(id)
        }
        ssboBuffer.flip()
        Gdx.gl31.glDeleteBuffers(3, ssboBuffer)
    }
}
