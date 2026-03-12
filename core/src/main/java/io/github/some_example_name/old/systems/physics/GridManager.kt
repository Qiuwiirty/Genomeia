package io.github.some_example_name.old.systems.physics

import io.github.some_example_name.old.systems.physics.PhysicsSystem.Companion.PARTICLE_MAX_RADIUS
import io.github.some_example_name.old.systems.simulation.ThreadManager.Companion.CHUNK_SIZE
import io.github.some_example_name.old.systems.simulation.ThreadManager.Companion.getChunkId
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import java.util.BitSet

class GridManager (
    var gridCellWidthSize: Int = WORLD_CELL_WIDTH,
    var gridCellHeightSize: Int = WORLD_CELL_HEIGHT,
) {

    var gridSize = gridCellWidthSize * gridCellHeightSize
    val grid = IntArray(gridSize * MAX_AMOUNT_OF_PARTICLES) { -3 }
    val particleCounts = ByteArray(gridSize)
    val isMoreThenMaxAmount = BitSet(gridSize)
    val mapMoreThenMax = Array(CHUNK_SIZE) { Int2ObjectOpenHashMap<IntArrayList>() }


    fun addCell(x: Int, y: Int, value: Int): Int {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) {
            return -1
        }
        val cellIndex = y * gridCellWidthSize + x
        val currentCount = particleCounts[cellIndex]

        if (currentCount >= MAX_AMOUNT_OF_PARTICLES) {
            println("MAX_AMOUNT_OF_CELLS")
            val threadId = getChunkId(cellIndex)
            var list = mapMoreThenMax[threadId].get(cellIndex)
            if (list == null) {
                list = IntArrayList()
                try {
                    mapMoreThenMax[threadId].put(cellIndex, list)
                } catch (e: Exception) {
                    println("threadId $threadId cellIndex $cellIndex")
                    throw e
                }

            }
            list.add(value)

            isMoreThenMaxAmount[cellIndex] = true
            particleCounts[cellIndex]++
            return cellIndex
        }

        val gridIndex = cellIndex * MAX_AMOUNT_OF_PARTICLES + currentCount
        grid[gridIndex] = value
        particleCounts[cellIndex]++
        return cellIndex
    }

    fun addCell(cellIndex: Int, value: Int): Int {
        val currentCount = particleCounts[cellIndex]

        if (currentCount >= MAX_AMOUNT_OF_PARTICLES) {
            println("MAX_AMOUNT_OF_CELLS")
            return -1 // Ячейка заполнена // The cell is filled
        }

        val gridIndex = cellIndex * MAX_AMOUNT_OF_PARTICLES + currentCount
        grid[gridIndex] = value
        particleCounts[cellIndex]++
        return cellIndex
    }

    // Удалить элемент из ячейки (x, y) по значению (если порядок не важен)
    // Remove element from cell (x, y) by value (if order doesn't matter)
    fun removeCell(x: Int, y: Int, value: Int) {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) return

        val cellIndex = y * gridCellWidthSize + x
        val start = cellIndex * MAX_AMOUNT_OF_PARTICLES

        // Сколько слотов в основном массиве grid нужно просматривать
        val searchCount = if (isMoreThenMaxAmount[cellIndex])
            MAX_AMOUNT_OF_PARTICLES
        else
            particleCounts[cellIndex].toInt()

        val end = start + searchCount - 1

        // 1. Ищем в основном массиве grid
        for (i in start..end) {
            if (grid[i] == value) {

                if (isMoreThenMaxAmount[cellIndex]) {
                    // Переполнение — берём последний элемент из списка переполнения
                    val threadId = getChunkId(cellIndex)
                    val list = mapMoreThenMax[threadId].get(cellIndex)

                    if (list != null && !list.isEmpty) {
                        val movedValue = list.removeInt(list.size - 1)
                        grid[i] = movedValue

                        if (list.isEmpty) {
                            isMoreThenMaxAmount[cellIndex] = false
                            mapMoreThenMax[threadId].remove(cellIndex)
                        }
                    }
                } else {
                    // Обычный случай — swap-remove (КЛАССИЧЕСКИЙ)
                    grid[i] = grid[end]
                    // ← УБРАЛИ grid[end] = -2 ! Это и было источником проблемы
                }

                particleCounts[cellIndex]--
                return
            }
        }

        // 2. Не нашли в grid → ищем в списке переполнения
        if (isMoreThenMaxAmount[cellIndex]) {
            val threadId = getChunkId(cellIndex)
            val list = mapMoreThenMax[threadId].get(cellIndex)

            if (list != null && list.rem(value)) {
                if (list.isEmpty) {
                    isMoreThenMaxAmount[cellIndex] = false
                    mapMoreThenMax[threadId].remove(cellIndex)
                }
                particleCounts[cellIndex]--
            }
        }
    }

    // Получить все элементы ячейки (x, y) // Get all cell elements (x, y)
    fun getParticles(x: Int, y: Int): IntArray {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) {
            return IntArray(0)
        }

        val cellIndex = y * gridCellWidthSize + x
        return getParticlesIndex(cellIndex)
    }

    fun getParticlesIndex(cellIndex: Int): IntArray {
        val start = cellIndex * MAX_AMOUNT_OF_PARTICLES
        val count = if (particleCounts[cellIndex] >= MAX_AMOUNT_OF_PARTICLES)
            MAX_AMOUNT_OF_PARTICLES
        else
            particleCounts[cellIndex].toInt()

        return if (!isMoreThenMaxAmount[cellIndex]) {
            grid.copyOfRange(start, start + count)
        } else {
            val threadId = getChunkId(cellIndex)
            val extraList = mapMoreThenMax[threadId].get(cellIndex)
            val extraSize = extraList?.size ?: 0

            IntArray(count + extraSize).apply {
                if (extraSize > 0) System.arraycopy(extraList!!.elements(), 0, this, 0, extraSize)
                System.arraycopy(grid, start, this, extraSize, count)
            }
        }
    }

    fun clearAll() {
        particleCounts.fill(0)
    }

    companion object {
        var WORLD_SIZE_TYPE = WorldSize.XL
        val WORLD_CELL_WIDTH = WORLD_SIZE_TYPE.size
        val WORLD_CELL_HEIGHT = WORLD_SIZE_TYPE.size
        val GRID_SIZE = WORLD_CELL_WIDTH * WORLD_CELL_HEIGHT
        const val CELL_SIZE = PARTICLE_MAX_RADIUS * 2
        const val MAX_AMOUNT_OF_PARTICLES = 4
    }
}

//TODO Any sizes and aspect ratios
enum class WorldSize(val size: Int, val threadCount: Int, val generateWorldSize: Int, val maxZoom: Float = 0.2f ) {
    S(24, 2, 37, 1f), //Small (маленький)

    M(48, 4, 75, 0.5f), //Medium (средний)

    L(96, 6, 153, 0.25f), //Large (большой)

    XL(192 * 5, 6, 305, 0.125f), //Extra Large (очень большой)
}
