package io.github.some_example_name.old.systems.physics

import io.github.some_example_name.old.systems.physics.PhysicsSystem.Companion.PARTICLE_MAX_RADIUS


//TODO оценить масштаб проблемы grid based оптимизации
// TODO: Assess the scale of the grid-based optimization problem
class GridManager (
    var gridCellWidthSize: Int = WORLD_CELL_WIDTH,
    var gridCellHeightSize: Int = WORLD_CELL_HEIGHT,
) {

    var GRID_SIZE = gridCellWidthSize * gridCellHeightSize
    var WORLD_WIDTH = gridCellWidthSize * CELL_SIZE
    var WORLD_HEIGHT = gridCellHeightSize * CELL_SIZE
    var WORLD_WIDTH_MINUS_CELL_RADIUS = WORLD_WIDTH - PARTICLE_MAX_RADIUS
    var WORLD_HEIGHT_MINUS_CELL_RADIUS = WORLD_HEIGHT - PARTICLE_MAX_RADIUS

    //TODO возможно будет лучше: Off-heap хранение (ByteBuffer) Для очень больших сеток (если важно избегать GC)
    // TODO might be better: Off-heap storage (ByteBuffer) For very large grids (if avoiding GC is important)
    val grid = IntArray(GRID_SIZE * MAX_AMOUNT_OF_CELLS) { -1 }
    val cellCounts = IntArray(GRID_SIZE) // Счетчик элементов в каждой ячейке // Count of elements in each cell

    // Добавить элемент в ячейку (x, y) // Add an element to cell (x, y)
    fun addCell(x: Int, y: Int, value: Int/*, killCell: () -> Unit*/): Int {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) {
//            killCell.invoke()
            return -1
        }
        val cellIndex = y * gridCellWidthSize + x
        val currentCount = cellCounts[cellIndex]

        if (currentCount >= MAX_AMOUNT_OF_CELLS) {
            println("MAX_AMOUNT_OF_CELLS")
//            killCell.invoke()
            return -1 // Ячейка заполнена
        }

        val gridIndex = cellIndex * MAX_AMOUNT_OF_CELLS + currentCount
        grid[gridIndex] = value
        cellCounts[cellIndex]++
        return cellIndex
    }

    fun addCell(cellIndex: Int, value: Int): Int {
        val currentCount = cellCounts[cellIndex]

        if (currentCount >= MAX_AMOUNT_OF_CELLS) {
            println("MAX_AMOUNT_OF_CELLS")
            return -1 // Ячейка заполнена // The cell is filled
        }

        val gridIndex = cellIndex * MAX_AMOUNT_OF_CELLS + currentCount
        grid[gridIndex] = value
        cellCounts[cellIndex]++
        return cellIndex
    }

    // Удалить элемент из ячейки (x, y) по значению (если порядок не важен)
    // Remove element from cell (x, y) by value (if order doesn't matter)
    fun removeCell(x: Int, y: Int, value: Int): Boolean {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) return false
        val cellIndex = y * gridCellWidthSize + x
        val start = cellIndex * MAX_AMOUNT_OF_CELLS
        val end = start + cellCounts[cellIndex] - 1

        for (i in start..end) {
            if (grid[i] == value) {
                // Заменяем удаляемый элемент последним в ячейке
                // Replace the deleted element with the last one in the cell
                grid[i] = grid[end]
                grid[end] = -1
                cellCounts[cellIndex]--
                return true
            }
        }
        return false // Элемент не найден // Element not found
    }

    fun getCellsCount(x: Int, y: Int): Int {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) {
            return 0
        }
        val cellIndex = y * gridCellWidthSize + x
        return cellCounts[cellIndex]
    }

    // Получить все элементы ячейки (x, y) // Get all cell elements (x, y)
    fun getCells(x: Int, y: Int): IntArray {
        if (x < 0 || x >= gridCellWidthSize || y < 0 || y >= gridCellHeightSize) {
            return IntArray(0) // Возвращаем пустой массив вместо списка // Return an empty array instead of a list
        }

        val cellIndex = y * gridCellWidthSize + x
        val start = cellIndex * MAX_AMOUNT_OF_CELLS
        val count = cellCounts[cellIndex]

        // Создаем массив нужного размера и копируем данные // Create an array of the required size and copy the data
        return grid.copyOfRange(start, start + count)
    }

    fun getCells(cellIndex: Int): IntArray {
        val start = cellIndex * MAX_AMOUNT_OF_CELLS
        val count = cellCounts[cellIndex]

        // Создаем массив нужного размера и копируем данные // Create an array of the required size and copy the data
        return grid.copyOfRange(start, start + count)
    }

    fun clearAll() {
        cellCounts.fill(0)
    }

    companion object {
        var WORLD_SIZE_TYPE = WorldSize.XL
        val WORLD_CELL_WIDTH = WORLD_SIZE_TYPE.size
        val WORLD_CELL_HEIGHT = WORLD_SIZE_TYPE.size
        val GRID_SIZE = WORLD_CELL_WIDTH * WORLD_CELL_HEIGHT
        const val CELL_SIZE = PARTICLE_MAX_RADIUS * 2
        const val MAX_AMOUNT_OF_CELLS = 12
    }
}

//TODO Any sizes and aspect ratios
enum class WorldSize(val size: Int, val threadCount: Int, val generateWorldSize: Int, val maxZoom: Float = 0.2f ) {
    S(24, 2, 37, 1f), //Small (маленький)

    M(48, 4, 75, 0.5f), //Medium (средний)

    L(96, 6, 153, 0.25f), //Large (большой)

    XL(192, 6, 305, 0.125f), //Extra Large (очень большой)
}
