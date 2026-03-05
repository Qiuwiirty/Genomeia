package io.github.some_example_name.old.systems.render

import io.github.some_example_name.old.entities.ParticleEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference

class TripleBufferManager(
    val particleEntity: ParticleEntity
) {

    companion object {
        const val PARTICLE_MAX_AMOUNT = 1_000_000 //TODO сделать динамическое увеличение
        const val BUFFER_SIZE_BYTES = PARTICLE_MAX_AMOUNT * 16  // 16 bytes per particle: 3 floats + 1 int
    }

    private val bufferA: ByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE_BYTES).order(ByteOrder.nativeOrder())
    private val bufferB: ByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE_BYTES).order(ByteOrder.nativeOrder())
    private val bufferC: ByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE_BYTES).order(ByteOrder.nativeOrder())

    private val sharedBuffer: AtomicReference<ByteBuffer> = AtomicReference(bufferA)
    private var producerBuffer: ByteBuffer = bufferB
    private var consumerBuffer: ByteBuffer = bufferC

    private fun putBufferData() {
        producerBuffer.clear()
        with(particleEntity) {
            for (i in 0..particleLastId) {
                producerBuffer.putFloat(x[i])
                producerBuffer.putFloat(y[i])
                producerBuffer.putFloat(radius[i])
                producerBuffer.putInt(color[i])
            }
        }
        producerBuffer.flip()
    }

    // После обновления позиций симуляции (вызывается в потоке симуляции)
    fun updateAndCommitProducer() {
        putBufferData()
        // Атомарный своп producerBuffer с sharedBuffer
        val temp = sharedBuffer.getAndSet(producerBuffer)
        producerBuffer = temp
    }

    // Перед чтением данных в main thread (вызывается в потоке рендеринга)
    fun getAndSwapConsumer(): ByteBuffer {
        // Атомарный своп consumerBuffer с sharedBuffer
        val temp = sharedBuffer.getAndSet(consumerBuffer)
        consumerBuffer = temp
        consumerBuffer.rewind()
        return consumerBuffer
    }
}
