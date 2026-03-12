package io.github.some_example_name.old.systems.render

import io.github.some_example_name.old.entities.ParticleEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference

class TripleBufferManager(
    val particleEntity: ParticleEntity
) {

    companion object {
        const val PARTICLE_MAX_AMOUNT = 2_500_000
        const val BUFFER_SIZE_BYTES = PARTICLE_MAX_AMOUNT * 16
    }

    private val bufferA: ByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE_BYTES).order(ByteOrder.nativeOrder())
    private val bufferB: ByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE_BYTES).order(ByteOrder.nativeOrder())
    // bufferC больше не нужен — достаточно двойного буфера на CPU

    private val latestBuffer = AtomicReference(bufferA)   // буфер, который видит рендер
    private var writeBuffer: ByteBuffer = bufferB         // куда пишет симуляция

    private var lastReturnedBuffer: ByteBuffer? = null    // для правильного isNewFrame (только в render thread)

    private fun putBufferData() {
        writeBuffer.clear()
        with(particleEntity) {
            for (i in 0..particleLastId) {
                writeBuffer.putFloat(x[i])
                writeBuffer.putFloat(y[i])
                writeBuffer.putFloat(radius[i])
                writeBuffer.putInt(color[i])
            }
        }
        writeBuffer.flip()
    }

    // Вызывается ТОЛЬКО когда симуляция сделала шаг (как do_sync в твоём C++)
    fun updateAndCommitProducer() {
        putBufferData()

        // Публикуем новый буфер и забираем старый для следующей записи
        val old = latestBuffer.getAndSet(writeBuffer)
        writeBuffer = old
    }

    // Вызывается каждый кадр рендера
    fun getAndSwapConsumer(): Pair<ByteBuffer, Boolean> {
        val currentLatest = latestBuffer.get()

        val isNewFrame = currentLatest !== lastReturnedBuffer
        if (isNewFrame) {
            lastReturnedBuffer = currentLatest
        }

        currentLatest.rewind()
        return currentLatest to isNewFrame
    }
}
