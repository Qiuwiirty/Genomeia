package io.github.some_example_name.old.entities

import it.unimi.dsi.fastutil.ints.IntArrayList
import java.util.BitSet

abstract class Entity(startMaxAmount: Int) {
    protected var maxAmount = startMaxAmount
    var lastId = -1

    var deadStack = IntArrayList(startMaxAmount)

    var isAlive = BitSet(maxAmount)
    var generation = IntArray(maxAmount)

    protected fun add(): Int {
        val cellIndex = if (!deadStack.isEmpty()) {
            deadStack.removeInt(deadStack.size - 1)
        } else {
            ++lastId
        }
        isAlive[cellIndex] = true
        generation[cellIndex]++
        if (maxAmount - 2 < lastId) {
            resize()
        }
        return cellIndex
    }

    protected fun delete(index: Int) {
        if (!isAlive[index]) throw Exception("Already dead")
        isAlive[index] = false
        deadStack.add(index)
    }

    fun clear() {
        val cellBound = (lastId + 1).coerceAtLeast(0)
        lastId = -1
        deadStack.clear()
        generation.fill(0, 0, cellBound)
        isAlive.clear()

        onClear(cellBound)
    }

    fun resize() {
        val oldMax = maxAmount
        maxAmount = (oldMax * 5 / 4).coerceAtLeast(oldMax + 1)
        run {
            val old = generation
            generation = IntArray(maxAmount)
            System.arraycopy(old, 0, generation, 0, oldMax)
        }
        run {
            val old = isAlive
            isAlive = BitSet(maxAmount)
            isAlive.or(old)
        }
        onResize(oldMax)
    }

    protected abstract fun onCopy()
    protected abstract fun onPaste()
    protected abstract fun onClear(bound: Int)
    protected abstract fun onResize(oldMax: Int)
}
