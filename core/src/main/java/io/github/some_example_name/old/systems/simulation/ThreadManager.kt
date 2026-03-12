package io.github.some_example_name.old.systems.simulation

import io.github.some_example_name.old.entities.SimEntity
import io.github.some_example_name.old.systems.physics.GridManager.Companion.GRID_SIZE
import io.github.some_example_name.old.systems.physics.GridManager.Companion.WORLD_CELL_HEIGHT
import io.github.some_example_name.old.systems.simulation.SimulationSystem.Companion.DELTA_SIM_TICK_TIME
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ThreadManager(
    val simEntity: SimEntity
) {

    val executor = Executors.newFixedThreadPool(THREAD_COUNT)
    val futures = mutableListOf<Future<*>>()

    var isRunning = false

    // Запуск потока // Starting a thread
//    fun startUpdateThread() {
//        if (!isRunning) {
//            isRunning = true
//            updateThread.start()
//        }
//    }

    // Остановка потока // Stopping a thread
//    fun stopUpdateThread() {
//        isRunning = false
//        updateThread.interrupt()  // Прерываем sleep, если есть // Interrupt sleep if there is one
//    }

    fun dispose() {
        // 1. Остановить главный поток // Stop the main thread
        isRunning = false


        // 2. Остановить executor // Stop the executor
        executor.shutdown()
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }

        futures.clear()
    }

    // Основной цикл в отдельном потоке // Main loop in a separate thread
    inline fun runUpdateLoop(onUpdateTick: () -> Unit) {
        var lastTime = System.nanoTime()
        var accumulator = 0.0
        var wasMaxSpeed = false

        // для UPS // for UPS
        var updatesThisSecond = 0
        var lastUpsTime = System.nanoTime()

        try {
            while (isRunning) {
                if (simEntity.isPlay) {
                    val currentTime = System.nanoTime()
                    var frameTime = (currentTime - lastTime) / 1_000_000_000.0
                    lastTime = currentTime

                    // Сброс при выходе из maxSpeed // Reset when exiting maxSpeed
                    if (wasMaxSpeed && !simEntity.maxSpeed) {
                        accumulator = 0.0
                        frameTime = 0.0
                        wasMaxSpeed = false
                    }

                    val clampedFrameTime = minOf(frameTime, 0.25)
                    accumulator += clampedFrameTime

                    if (simEntity.maxSpeed) {
                        wasMaxSpeed = true
                        onUpdateTick.invoke()
                        updatesThisSecond++
                    } else {
                        // обычный фиксированный timestep // normal fixed timestep
                        var didUpdate = false
                        while (accumulator >= DELTA_SIM_TICK_TIME) {
                            onUpdateTick.invoke()
//                            simulationSystem.updateTick()
//                            synchronized(simulationSystem) {
//                                simulationSystem.updateAfterCycle()
//                            }
                            accumulator -= DELTA_SIM_TICK_TIME
                            updatesThisSecond++
                            didUpdate = true
                        }

                        // спим остаток кадра // we sleep for the rest of the frame
                        if (didUpdate) {
                            val elapsed = (System.nanoTime() - currentTime) / 1_000_000_000.0
                            val sleepTime = DELTA_SIM_TICK_TIME - elapsed
                            if (sleepTime > 0) {
                                try {
                                    Thread.sleep(
                                        (sleepTime * 1000).toLong(),
                                        ((sleepTime * 1_000_000) % 1000_000).toInt()
                                    )
                                } catch (_: InterruptedException) {
                                    break
                                }
                            }
                        }
                    }

                    // === UPS вывод каждые 1 сек === UPS output every 1 sec ===
                    val now = System.nanoTime()
                    if ((now - lastUpsTime) >= 1_000_000_000L) {
                        simEntity.ups = updatesThisSecond
                        updatesThisSecond = 0
                        lastUpsTime = now
                    }

                } else {
                    try {
                        Thread.sleep(16)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }
        } catch (_: InterruptedException) {
            // выход // exit
        }
    }

    inline fun runChunkStage(
        isOdd: Boolean,
        crossinline job: (start: Int, end: Int, threadId: Int) -> Unit
    ) {
        var threadCounter = 0
        val first = if (isOdd) 1 else 0
        for (i in first until TOTAL_CHUNKS step 2) {
            val start = i * CHUNK_SIZE
            val end = if (i == TOTAL_CHUNKS - 1) GRID_SIZE else (i + 1) * CHUNK_SIZE
            val threadId = threadCounter++
            futures.add(executor.submit { job(start, end, threadId) })
        }
        futures.forEach { it.get() }   // <- barrier for this stage
        futures.clear()
    }

    companion object {
        fun getThreadId(gridIndex: Int) = gridIndex / GRID_INDEX_DIVIDER
        fun getChunkId(gridIndex: Int) = gridIndex / CHUNK_SIZE
        val THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 4
        val TOTAL_CHUNKS = THREAD_COUNT * 2
        val CHUNK_SIZE = GRID_SIZE / TOTAL_CHUNKS
        val GRID_INDEX_DIVIDER = CHUNK_SIZE * 2
        val HALF_CHUNK_HEIGHT = (WORLD_CELL_HEIGHT / TOTAL_CHUNKS) / 2f
    }
}
