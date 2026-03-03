package io.github.some_example_name.old.entities

import io.github.some_example_name.old.systems.genomics.CellManager.Companion.MAX_LINK_AMOUNT
import io.github.some_example_name.old.systems.genomics.genome.CellAction
import java.util.BitSet

class CellEntity(
    cellsStartMaxAmount: Int,
    val particleEntity: ParticleEntity,
    val simEntity: SimEntity
): Entity {
    var cellMaxAmount = cellsStartMaxAmount
    var cellLastId = -1

    var deadCellsStackAmount = -1
    var deadCellsStack = IntArray(250_000) { -1 }


    //Cell - genomic
    var isAliveCell = BitSet(cellMaxAmount)
    var cellGeneration = IntArray(cellMaxAmount)

    private var particleIndex = IntArray(cellMaxAmount) { -1 }

    //TODO сделать для Particle полей более удобный доступ
    fun getX(index: Int) = particleEntity.x[particleIndex[index]]
    fun getY(index: Int) = particleEntity.y[particleIndex[index]]
    fun setX(index: Int, value: Float) { particleEntity.x[particleIndex[index]] = value }
    fun setY(index: Int, value: Float) { particleEntity.y[particleIndex[index]] = value }
    fun getVx(index: Int) = particleEntity.vx[particleIndex[index]]
    fun getVy(index: Int) = particleEntity.vy[particleIndex[index]]
    fun setVx(index: Int, value: Float) { particleEntity.vx[particleIndex[index]] = value }
    fun setVy(index: Int, value: Float) { particleEntity.vy[particleIndex[index]] = value }
    fun getDragCoefficient(index: Int) = particleEntity.dragCoefficient[particleIndex[index]]
    fun setDragCoefficient(index: Int, value: Byte) {
        particleEntity.dragCoefficient[particleIndex[index]] = value
    }
    fun getRadius(index: Int) = particleEntity.radius[particleIndex[index]]
    fun seRadius(index: Int, value: Float) { particleEntity.radius[particleIndex[index]] = value }
    fun getGridId(index: Int) = particleEntity.gridId[particleIndex[index]]
    fun seGridId(index: Int, value: Int) { particleEntity.gridId[particleIndex[index]] = value }

    fun getTime(index: Int) = simEntity.timeSimulation


    var cellGenomeId = IntArray(cellMaxAmount) { -1 }
    var cellActions: Array<CellAction?> = arrayOfNulls(cellMaxAmount)
    var organismIndex = IntArray(cellMaxAmount) { -1 }
    var parentIndex = IntArray(cellMaxAmount) { -1 }
    var angle = FloatArray(cellMaxAmount)
    var angleDiff = FloatArray(cellMaxAmount)
    var energyNecessaryToDivide = FloatArray(cellMaxAmount) { 2f }  // TODO ByteArray 256 значений должно хватить
    var energyNecessaryToMutate = FloatArray(cellMaxAmount) { 1f }  // TODO ByteArray 256 значений должно хватить
    var isDividedInThisStage = BitSet(cellMaxAmount)
    var isMutateInThisStage = BitSet(cellMaxAmount)

    var color = IntArray(cellMaxAmount)
    var cellType = IntArray(cellMaxAmount) //TODO перевести в ByteArray возможно будет более оптимизированная проверка
    var energy = FloatArray(cellMaxAmount) //Рисовать


    var linksAmount = IntArray(cellMaxAmount) //TODO перевести в ByteArray возможно будет более оптимизированная проверка
    var links = IntArray(cellMaxAmount * MAX_LINK_AMOUNT) { -1 }//TODO It weighs a lot, almost as much as all the others combined - need to think how to get rid of it

    fun addLink(cellId: Int, linkId: Int) {
        val base = cellId * MAX_LINK_AMOUNT
        if (cellId < 0) return
        val amount = linksAmount[cellId]
        if (amount >= MAX_LINK_AMOUNT) {
            // перезаписываем последний
            links[base + MAX_LINK_AMOUNT - 1] = linkId
        } else {
            links[base + amount] = linkId
            linksAmount[cellId] += 1
        }
    }

    fun deleteLinkedCellLink(cellId: Int, linkId: Int) {
        val base = cellId * MAX_LINK_AMOUNT
        val amount = linksAmount[cellId]
        if (amount == 0) return

        for (i in 0 until amount) {
            val idx = base + i
            if (links[idx] == linkId) {
                // Заменяем на последний элемент
                links[idx] = links[base + amount - 1]
                links[base + amount - 1] = -1 // не обязательно, но может быть полезно
                linksAmount[cellId] -= 1
                return
            }
        }
    }

    //Cell - Neural
    var neuronImpulseInput = FloatArray(cellMaxAmount)
    var neuronImpulseOutput = FloatArray(cellMaxAmount)
    var isNeuronTransportable = BitSet(cellMaxAmount)
    var activationFuncType = IntArray(cellMaxAmount)
    var a = FloatArray(cellMaxAmount) { 1f }
    var b = FloatArray(cellMaxAmount)
    var c = FloatArray(cellMaxAmount)
    var dTime = FloatArray(cellMaxAmount) { -1f }
    var remember = FloatArray(cellMaxAmount)
    var isSum = BooleanArray(cellMaxAmount) { true }


    //Eye
    var colorDifferentiation = IntArray(cellMaxAmount) { 7 }
    var visibilityRange = FloatArray(cellMaxAmount) { 170f }

    override fun copy() {
        TODO("Not yet implemented")
    }

    override fun paste() {
        TODO("Not yet implemented")
    }


    override fun clear() {
        // Store the bounds for resetting arrays
        val cellBound = (cellLastId + 1).coerceAtLeast(0)

        // Reset last ID counters
        cellLastId = -1

        deadCellsStack.fill(-1, 0, (deadCellsStackAmount + 1).coerceAtLeast(0))
        deadCellsStackAmount = -1

        // Reset cell-related arrays (0 to cellLastId + 1)
        cellGeneration.fill(0, 0, cellBound)
        isAliveCell.clear()

        particleIndex.fill(-1, 0, cellBound)

        cellGenomeId.fill(-1, 0, cellBound)
        cellActions.fill(null, 0, cellBound)
        organismIndex.fill(-1, 0, cellBound)
        parentIndex.fill(-1, 0, cellBound)

        angle.fill(0f, 0, cellBound)

        color.fill(0, 0, cellBound)

        energyNecessaryToDivide.fill(2f, 0, cellBound)
        energyNecessaryToMutate.fill(1f, 0, cellBound)
        neuronImpulseInput.fill(0f, 0, cellBound)
        neuronImpulseOutput.fill(0f, 0, cellBound)
        isNeuronTransportable.clear()
        isDividedInThisStage.clear()
        isMutateInThisStage.clear()
        cellType.fill(0, 0, cellBound)
        energy.fill(0f, 0, cellBound)
        linksAmount.fill(0, 0, cellBound)
        links.fill(-1, 0, cellBound * MAX_LINK_AMOUNT)

        // Reset neural-related arrays (0 to cellLastId + 1)
        activationFuncType.fill(0, 0, cellBound)
        a.fill(1f, 0, cellBound)
        b.fill(0f, 0, cellBound)
        c.fill(0f, 0, cellBound)
        dTime.fill(-1f, 0, cellBound)
        remember.fill(0f, 0, cellBound)
        isSum.fill(true, 0, cellBound)

        // Reset directed-related arrays (0 to cellLastId + 1)
        angleDiff.fill(0f, 0, cellBound)

        // Reset eye-related arrays (0 to cellLastId + 1)
        colorDifferentiation.fill(7, 0, cellBound)
        visibilityRange.fill(170f, 0, cellBound)
    }

    override fun resize() {
        val oldMax = cellMaxAmount
        cellMaxAmount = (oldMax * 5 / 4).coerceAtLeast(oldMax + 1)

        // Resize cell arrays
        run {
            val old = cellGeneration
            cellGeneration = IntArray(cellMaxAmount)
            System.arraycopy(old, 0, cellGeneration, 0, oldMax)
        }
        run {
            val old = isAliveCell
            isAliveCell = BitSet(cellMaxAmount)
            isAliveCell.or(old)
        }
        run {
            val old = particleIndex
            particleIndex = IntArray(cellMaxAmount) { -1 }
            System.arraycopy(old, 0, particleIndex, 0, oldMax)
        }
        run {
            val old = cellGenomeId
            cellGenomeId = IntArray(cellMaxAmount) { -1 }
            System.arraycopy(old, 0, cellGenomeId, 0, oldMax)
        }
        run {
            val old = cellActions
            cellActions = arrayOfNulls(cellMaxAmount)
            System.arraycopy(old, 0, cellActions, 0, oldMax)
        }
        run {
            val old = organismIndex
            organismIndex = IntArray(cellMaxAmount) { -1 }
            System.arraycopy(old, 0, organismIndex, 0, oldMax)
        }
        run {
            val old = parentIndex
            parentIndex = IntArray(cellMaxAmount) { -1 }
            System.arraycopy(old, 0, parentIndex, 0, oldMax)
        }
        run {
            val old = angle
            angle = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, angle, 0, oldMax)
        }
        run {
            val old = color
            color = IntArray(cellMaxAmount)
            System.arraycopy(old, 0, color, 0, oldMax)
        }
        run {
            val old = energyNecessaryToDivide
            energyNecessaryToDivide = FloatArray(cellMaxAmount) { 2f }
            System.arraycopy(old, 0, energyNecessaryToDivide, 0, oldMax)
        }
        run {
            val old = energyNecessaryToMutate
            energyNecessaryToMutate = FloatArray(cellMaxAmount) { 1f }
            System.arraycopy(old, 0, energyNecessaryToMutate, 0, oldMax)
        }
        run {
            val old = neuronImpulseInput
            neuronImpulseInput = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, neuronImpulseInput, 0, oldMax)
        }
        run {
            val old = neuronImpulseOutput
            neuronImpulseOutput = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, neuronImpulseOutput, 0, oldMax)
        }
        run {
            val old = isNeuronTransportable
            isNeuronTransportable = BitSet(cellMaxAmount)
            isNeuronTransportable.or(old)
        }
        run {
            val old = isDividedInThisStage
            isDividedInThisStage = BitSet(cellMaxAmount)
            System.arraycopy(old, 0, isDividedInThisStage, 0, oldMax)
        }
        run {
            val old = isMutateInThisStage
            isMutateInThisStage = BitSet(cellMaxAmount)
            isMutateInThisStage.or(old)
        }
        run {
            val old = cellType
            cellType = IntArray(cellMaxAmount)
            System.arraycopy(old, 0, cellType, 0, oldMax)
        }
        run {
            val old = energy
            energy = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, energy, 0, oldMax)
        }
        run {
            val old = linksAmount
            linksAmount = IntArray(cellMaxAmount)
            System.arraycopy(old, 0, linksAmount, 0, oldMax)
        }
        run {
            val old = activationFuncType
            activationFuncType = IntArray(cellMaxAmount)
            System.arraycopy(old, 0, activationFuncType, 0, oldMax)
        }
        run {
            val old = a
            a = FloatArray(cellMaxAmount) { 1f }
            System.arraycopy(old, 0, a, 0, oldMax)
        }
        run {
            val old = b
            b = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, b, 0, oldMax)
        }
        run {
            val old = c
            c = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, c, 0, oldMax)
        }
        run {
            val old = dTime
            dTime = FloatArray(cellMaxAmount) { -1f }
            System.arraycopy(old, 0, dTime, 0, oldMax)
        }
        run {
            val old = remember
            remember = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, remember, 0, oldMax)
        }
        run {
            val old = isSum
            isSum = BooleanArray(cellMaxAmount) { true }
            System.arraycopy(old, 0, isSum, 0, oldMax)
        }
        run {
            val old = angleDiff
            angleDiff = FloatArray(cellMaxAmount)
            System.arraycopy(old, 0, angleDiff, 0, oldMax)
        }
        run {
            val old = colorDifferentiation
            colorDifferentiation = IntArray(cellMaxAmount) { 7 }
            System.arraycopy(old, 0, colorDifferentiation, 0, oldMax)
        }
        run {
            val old = visibilityRange
            visibilityRange = FloatArray(cellMaxAmount) { 170f }
            System.arraycopy(old, 0, visibilityRange, 0, oldMax)
        }

        // Special handling for links array
        run {
            val oldLinks = links
            links = IntArray(cellMaxAmount * MAX_LINK_AMOUNT) { -1 }
            for (i in 0 until oldMax) {
                System.arraycopy(oldLinks, i * MAX_LINK_AMOUNT, links, i * MAX_LINK_AMOUNT, MAX_LINK_AMOUNT)
            }
        }
    }

    fun deleteCell(cellIndex: Int) {
        TODO()
//    if (!isAliveCell[cellIndex]) return
//
//    if (organismIndex[cellIndex] != -1) {
//
//        if (!isDividedInThisStage[cellIndex]) {
//            val organism = organismManager.organisms[organismIndex[cellIndex]]
//            organism.divideCounterThisStage--
//        }
//
//        if (!isMutateInThisStage[cellIndex]) {
//            val organism = organismManager.organisms[organismIndex[cellIndex]]
//            organism.mutateCounterThisStage--
//        }
//    }
//
//    val gridX = (x[cellIndex] / CELL_SIZE).toInt()
//    val gridY = (y[cellIndex] / CELL_SIZE).toInt()
//    gridManager.removeCell(gridX, gridY, cellIndex)
//
//    isAliveCell[cellIndex] = false
//
//    deadCellsStackAmount ++
//    deadCellsStack[deadCellsStackAmount] = cellIndex
//
//    cellGenomeId[cellIndex] = -1
//    parentIndex[cellIndex] = -1
//    cellActions[cellIndex] = null
//    organismIndex[cellIndex] = -1
//    gridId[cellIndex] = -1
//    x[cellIndex] = 0f
//    y[cellIndex] = 0f
//    angle[cellIndex] = 0f
//    vx[cellIndex] = 0f
//    vy[cellIndex] = 0f
//    vxOld[cellIndex] = 0f
//    vyOld[cellIndex] = 0f
//    ax[cellIndex] = 0f
//    ay[cellIndex] = 0f
//    colorR[cellIndex] = 1f
//    colorG[cellIndex] = 1f
//    colorB[cellIndex] = 1f
//    energyNecessaryToDivide[cellIndex] = 2f
//    energyNecessaryToMutate[cellIndex] = 1f
//    neuronImpulseInput[cellIndex] = 0f
//    neuronImpulseOutput[cellIndex] = 0f
//    dragCoefficient[cellIndex] = 0.93f
//    isAliveWithoutEnergy[cellIndex] = 200
//    isNeuronTransportable[cellIndex] = true
//    effectOnContact[cellIndex] = false
//    isDividedInThisStage[cellIndex] = true
//    isMutateInThisStage[cellIndex] = true
//    cellType[cellIndex] = 0
//    energy[cellIndex] = 0f
//    tickRestriction[cellIndex] = 0
//
//    while (linksAmount[cellIndex] > 0) {
//        val base = cellIndex * MAX_LINK_AMOUNT
//        val linkId = links[base + 0]
//        if (linkId != -1) {
//            deleteLink(linkId)
//        }
//    }
//
//    linksAmount[cellIndex] = 0
//    val base = cellIndex * MAX_LINK_AMOUNT
//    links.fill(-1, base, base + MAX_LINK_AMOUNT)
//
//    activationFuncType[cellIndex] = 0
//    a[cellIndex] = 1f
//    b[cellIndex] = 0f
//    c[cellIndex] = 0f
//    dTime[cellIndex] = -1f
//    remember[cellIndex] = 0f
//    isSum[cellIndex] = true
//    angleDiff[cellIndex] = 0f
//    colorDifferentiation[cellIndex] = 7
//    visibilityRange[cellIndex] = 170f
//    speed[cellIndex] = 0f
    }

}
