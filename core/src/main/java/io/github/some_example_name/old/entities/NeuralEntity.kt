package io.github.some_example_name.old.entities

import io.github.some_example_name.old.cells.Cell
import java.util.BitSet
import kotlin.collections.fill

class NeuralEntity(
    neuralStartMaxAmount: Int,
    val cellList: List<Cell>
): Entity(neuralStartMaxAmount) {

    var neuronImpulseInput = FloatArray(maxAmount)
    var neuronImpulseOutput = FloatArray(maxAmount)
    var isNeuronTransportable = BitSet(maxAmount)
    var activationFuncType = ByteArray(maxAmount)
    var a = FloatArray(maxAmount) { 1f }
    var b = FloatArray(maxAmount)
    var c = FloatArray(maxAmount)
    var dTime = FloatArray(maxAmount) { -1f }
    var remember = FloatArray(maxAmount)
    var isSum = BitSet(maxAmount)

    fun addNeural(
        cellType: Int,
        a: Float = 1f,
        b: Float = 0f,
        c: Float = 0f,
        isSum: Boolean,
        activationFuncType: Byte
    ): Int {
        val neuralIndex = add()

        this.neuronImpulseInput[neuralIndex] = 0f
        this.neuronImpulseOutput[neuralIndex] = 0f
        this.isNeuronTransportable[neuralIndex] = cellList[cellType].isNeuronTransportable
        this.activationFuncType[neuralIndex] = activationFuncType
        this.a[neuralIndex] = a
        this.b[neuralIndex] = b
        this.c[neuralIndex] = c
        this.dTime[neuralIndex] = -1f
        this.remember[neuralIndex]  = 0f
        this.isSum[neuralIndex] = isSum
        return neuralIndex
    }

    fun deleteNeural(neuralIndex: Int) {
        delete(neuralIndex)

        neuronImpulseInput[neuralIndex] = 0f
        neuronImpulseOutput[neuralIndex] = 0f
        isNeuronTransportable[neuralIndex] = true
        activationFuncType[neuralIndex] = 0
        a[neuralIndex] = 1f
        b[neuralIndex] = 0f
        c[neuralIndex] = 0f
        dTime[neuralIndex] = -1f
        remember[neuralIndex] = 0f
        isSum[neuralIndex] = true
    }

    override fun onCopy() {
        TODO("Not yet implemented")
    }

    override fun onPaste() {
        TODO("Not yet implemented")
    }

    override fun onClear(bound: Int) {
        neuronImpulseInput.fill(0f, 0, bound)
        neuronImpulseOutput.fill(0f, 0, bound)
        isNeuronTransportable.clear()
        activationFuncType.fill(0, 0, bound)
        a.fill(1f, 0, bound)
        b.fill(0f, 0, bound)
        c.fill(0f, 0, bound)
        dTime.fill(-1f, 0, bound)
        remember.fill(0f, 0, bound)
        isSum.clear()
    }

    override fun onResize(oldMax: Int) {
        run {
            val old = neuronImpulseInput
            neuronImpulseInput = FloatArray(maxAmount)
            System.arraycopy(old, 0, neuronImpulseInput, 0, oldMax)
        }
        run {
            val old = neuronImpulseOutput
            neuronImpulseOutput = FloatArray(maxAmount)
            System.arraycopy(old, 0, neuronImpulseOutput, 0, oldMax)
        }
        run {
            val old = isNeuronTransportable
            isNeuronTransportable = BitSet(maxAmount)
            isNeuronTransportable.or(old)
        }
        run {
            val old = activationFuncType
            activationFuncType = ByteArray(maxAmount)
            System.arraycopy(old, 0, activationFuncType, 0, oldMax)
        }
        run {
            val old = a
            a = FloatArray(maxAmount) { 1f }
            System.arraycopy(old, 0, a, 0, oldMax)
        }
        run {
            val old = b
            b = FloatArray(maxAmount)
            System.arraycopy(old, 0, b, 0, oldMax)
        }
        run {
            val old = c
            c = FloatArray(maxAmount)
            System.arraycopy(old, 0, c, 0, oldMax)
        }
        run {
            val old = dTime
            dTime = FloatArray(maxAmount) { -1f }
            System.arraycopy(old, 0, dTime, 0, oldMax)
        }
        run {
            val old = remember
            remember = FloatArray(maxAmount)
            System.arraycopy(old, 0, remember, 0, oldMax)
        }
        run {
            val old = isSum
            isSum = BitSet(maxAmount)
            System.arraycopy(old, 0, isSum, 0, oldMax)
        }
    }
}
