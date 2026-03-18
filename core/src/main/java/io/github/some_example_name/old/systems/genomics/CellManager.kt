package io.github.some_example_name.old.systems.genomics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import io.github.some_example_name.old.cells.Cell
import io.github.some_example_name.old.cells.Eye
import io.github.some_example_name.old.cells.Muscle
import io.github.some_example_name.old.cells.base.activation
import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.commands.WorldCommandType
import io.github.some_example_name.old.core.utils.collectCells
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.OrganEntity
import io.github.some_example_name.old.systems.genomics.genome.GenomeManager
import io.github.some_example_name.old.systems.physics.GridManager
import kotlin.math.PI
import kotlin.math.atan2

class CellManager(
    val cellEntity: CellEntity,
    val linkEntity: LinkEntity,
    val organEntity: OrganEntity,
    val genomeManager: GenomeManager,
    val commandsManager: CommandsManager,
    val cellList: List<Cell>,
    val gridManager: GridManager
) {

    fun iterateCell() {
        //TODO process parallel
        for (cellIndex in 0..cellEntity.lastId) {
            processCell(cellIndex, threadId = 0)
        }
    }

    private fun processCell(
        cellIndex: Int,
        threadId: Int
    ) = with(cellEntity) {
        if (!isAlive[cellIndex]) return

        if (energy[cellIndex] <= 0f) commandsManager.worldCommandBuffer[threadId].push(
            type = WorldCommandType.DELETE_CELL,
            ints = intArrayOf(cellIndex)
        )

        if (getIsNeuronTransportable(cellIndex)) {
            //TODO Add a flag to perform impulse calculations only for cells through which a neural impulse passes!!!
            //TODO Вообще не нравится эта конструкция
            val impulse = cellList[cellType[cellIndex].toInt()].activation(
                cellIndex,
                getNeuronImpulseInput(cellIndex)
            )
            setNeuronImpulseOutput(cellIndex, impulse)
        }

        cellList[cellType[cellIndex].toInt()].doOnTick(index = cellIndex, threadId = threadId)

        setNeuronImpulseInput(cellIndex, if (getIsSum(cellIndex)) 0f else 1f)

        genomicTransformations(cellIndex, threadId)
        processCellAngle(cellIndex)
    }

    //TODO Calculate the angle only relative to the parent cell, and only for the cells where it’s necessary, or only at the moment of division
    private fun processCellAngle(cellIndex: Int) = with(cellEntity) {
        if (parentIndex[cellIndex] != -1) {
            val linkId = linkEntity.linkIndexMap.get(cellIndex, parentIndex[cellIndex])
            if (linkId == -1) return //TODO потетсить всякие варинаты без этой защиты
            val c1 = linkEntity.links1[linkId]
            val c2 = linkEntity.links2[linkId]
            val childCellIndex = if (cellIndex != c2) c2 else c1

            val dx = getX(cellIndex) - getX(childCellIndex)
            val dy = getY(cellIndex) - getY(childCellIndex)
            val angleToChild = atan2(dy, dx)

            angle[cellIndex] = angleToChild + angleDiff[cellIndex]
        }
    }

    private fun genomicTransformations(cellIndex: Int, threadId: Int) = with(cellEntity) {
        val organIndex = organIndex[cellIndex]
        if (!organEntity.alreadyGrownUp[organIndex]) {
            if (organEntity.justChangedStage[organIndex]) {
                val currentStage = genomeManager.genomes[organEntity.genomeIndex[organIndex]]
                    .genomeStageInstruction[organEntity.stage[organIndex]]
                val action = currentStage.cellActions[cellGenomeId[cellIndex]]
                val isDivideNotNull = action?.divide != null
                val isMutateNotNull = action?.mutate != null

                cellActions[cellIndex] = action

                isDividedInThisStage[cellIndex] = !isDivideNotNull
                isMutateInThisStage[cellIndex] = !isMutateNotNull

                if (isDivideNotNull) {
                    //TODO Make a more accurate energy calculation
                    energyNecessaryToDivide[cellIndex] = 2.5f
                    commandsManager.worldCommandBuffer[threadId].push(
                        type = WorldCommandType.DIVIDE_ALIVE_CELL_ACTION_COUNTER,
                        intArrayOf(organIndex)
                    )
                }

                if (isMutateNotNull) {
                    //TODO Make a more accurate energy calculation
                    energyNecessaryToMutate[cellIndex] = 1.2f
                    commandsManager.worldCommandBuffer[threadId].push(
                        type = WorldCommandType.MUTATE_ALIVE_CELL_ACTION_COUNTER,
                        intArrayOf(organIndex)
                    )
                }
            }
            mutateCell(cellIndex, threadId)
            divideCell(cellIndex, threadId)
        }
    }

    private fun mutateCell(index: Int, threadId: Int) = with(cellEntity) {
        if (!isMutateInThisStage[index] && energy[index] >= energyNecessaryToMutate[index]) {
            isMutateInThisStage[index] = true

            val action = cellActions[index]?.mutate ?: return

            commandsManager.worldCommandBuffer[threadId].push(
                type = WorldCommandType.DECREMENT_MUTATION_COUNTER,
                ints = intArrayOf(organIndex[index])
            )

            var isFromMuscleToAnother = false
            action.cellType?.let {
                val lastCellType = cellType[index].toInt()
                val lastCell = cellList[lastCellType]
                val newCell = cellList[it]
                isFromMuscleToAnother = lastCell is Muscle && newCell !is Muscle
                if (lastCell.isNeural && !newCell.isNeural) deleteNeural(index)
                if (!lastCell.isNeural && newCell.isNeural) addNeural(index, cellType = it)
                if (lastCell is Eye && newCell !is Eye) deleteEye(index)
                if (lastCell !is Eye && newCell is Eye) addEye(index)
                cellType[index] = it.toByte()
                setIsNeuronTransportable(index, false)
                setDragCoefficient(index, substrateSettings.data.viscosityOfTheEnvironment)
                setEffectOnContact(index, newCell.effectOnContact)
            }

            if (isFromMuscleToAnother) {
                //TODO придумать как выприямить все линки, если мышца превратиалсь в дргую клетку
            }

            action.color?.let { Color.rgba8888(it) }
            action.funActivation?.let { setActivationFuncType(index, it.toByte()) }
            action.a?.let { setA(index, it) }
            action.b?.let { setB(index, it) }
            action.c?.let { setC(index, it) }
            action.isSum?.let { setIsSum(index, it) }
            action.angleDirected?.let { angleDiff[index] = it }
            action.colorRecognition?.let { setColorDifferentiation(index, it.toByte()) }
            action.lengthDirected?.let { setVisibilityRange(index, it) }

            if (action.physicalLink.isNotEmpty()) {
                val gridX = getX(index).toInt()
                val gridY = getY(index).toInt()
                val closestCells = gridManager.collectCells(gridX, gridY)
                val idToIndexAssociation =
                    closestCells.filter { organIndex[it] == organIndex[index] && it != index }
                        .associateBy { cellGenomeId[it] }

                action.physicalLink.forEach { (cellGenomeIdToConnectWith, linkData) ->
                    val linkedCellIndex = idToIndexAssociation[cellGenomeIdToConnectWith]
                    if (linkedCellIndex != null) {
                        val linkIndex = linkEntity.linkIndexMap.get(index, linkedCellIndex)
                        if (linkData != null) {
                            if (linkIndex == -1) {
                                if (linkData.length != null) {
                                    if (linkData.isNeuronal && linkData.directedNeuronLink != cellGenomeId[index]
                                        && linkData.directedNeuronLink != cellGenomeIdToConnectWith
                                    ) {
                                        throw Exception("Incorrect logic in the neural-link")
                                    }

                                    val cellIndex: Int = index
                                    val otherCellIndex: Int = linkedCellIndex
                                    val linksLength: Float = linkData.length
                                    val degreeOfShortening: Float = 1f
                                    val isStickyLink: Boolean = false
                                    val isNeuronLink: Boolean = linkData.isNeuronal
                                    val isLink1NeuralDirected: Boolean = linkData.directedNeuronLink == cellGenomeId[index]

                                    commandsManager.worldCommandBuffer[threadId].push(
                                        type = WorldCommandType.ADD_LINK,
                                        booleans = booleanArrayOf(
                                            isStickyLink,
                                            isNeuronLink,
                                            isLink1NeuralDirected
                                        ),
                                        floats = floatArrayOf(linksLength, degreeOfShortening),
                                        ints = intArrayOf(cellIndex, otherCellIndex)
                                    )
                                }
                            } else {
                                with(linkEntity) {
                                    if (!linkData.isNeuronal) {
                                        val cellIndex = if (isLink1NeuralDirected[linkIndex]) links1[linkIndex] else links2[linkIndex]
                                        setNeuronImpulseInput(cellIndex, 0f)
                                        setNeuronImpulseOutput(cellIndex, 0f)
                                    } else {
                                        val cellLink1Index = links1[linkIndex]
                                        val cellLink2Index = links2[linkIndex]
                                        val cellLink1Id = cellGenomeId[cellLink1Index]
                                        val cellLink2Id = cellGenomeId[cellLink2Index]

                                        isLink1NeuralDirected[linkIndex] =
                                            linkData.directedNeuronLink == cellLink1Id

                                        if (linkData.directedNeuronLink != cellLink1Id && linkData.directedNeuronLink != cellLink2Id) {
                                            throw Exception("Incorrect logic in the neural-link ${linkData.directedNeuronLink} ${cellGenomeId[index]} ${cellGenomeIdToConnectWith}")
                                        }
                                    }

                                    isNeuronLink[linkIndex] = linkData.isNeuronal
                                }
                            }
                        } else {
                            if (linkIndex != -1) {
                                commandsManager.worldCommandBuffer[threadId].push(
                                    type = WorldCommandType.DELETE_LINK,
                                    ints = intArrayOf(linkIndex)
                                )
                            }
                        }
                    }
                }
            }

            energy[index] -= energyNecessaryToMutate[index]
        }
    }

    private fun divideCell(index: Int, threadId: Int) = with(cellEntity) {
        if (!isDividedInThisStage[index] && energy[index] >= energyNecessaryToDivide[index]) {
            isDividedInThisStage[index] = true

            val action = cellActions[index]?.divide ?: return

            run {
                val parentLinkLength = action.physicalLink[cellGenomeId[index]]?.length ?: 1f
                val genomeAngle = action.angle ?: throw Exception("Forgot angle")
                val divideAngle = genomeAngle + angle[index]
                val x: Float = getX(index) + MathUtils.cos(divideAngle) * parentLinkLength
                val y: Float = getY(index) + MathUtils.sin(divideAngle) * parentLinkLength
                val color: Int = Color.rgba8888(action.color ?: Color.WHITE)
                val radius: Float = 0.5f
                val cellGenomeId: Int = action.id
                val cellType: Int = action.cellType ?: throw Exception("Forgot cellType")
                val organismIndex: Int = if (cellType != 18) {
                    organIndex[index]
                } else TODO()
                val parentIndex: Int = index
                val angle: Float = divideAngle
                val angleDiff: Float = action.angleDirected ?: 0f
                val colorDifferentiation: Int = action.colorRecognition ?: 7
                val visibilityRange: Float = action.lengthDirected ?: 170f
                val a: Float = action.a ?: 1f
                val b: Float = action.b ?: 0f
                val c: Float = action.c ?: 0f
                val isSum: Boolean = action.isSum ?: true
                val activationFuncType: Int = action.funActivation ?: 0

                commandsManager.worldCommandBuffer[threadId].push(
                    type = WorldCommandType.ADD_CELL,
                    booleans = booleanArrayOf(isSum),
                    floats = floatArrayOf(x, y, radius, angle, angleDiff, visibilityRange, a, b, c),
                    ints = intArrayOf(
                        color,
                        cellGenomeId,
                        cellType,
                        organismIndex,
                        parentIndex,
                        colorDifferentiation,
                        activationFuncType
                    )
                )

                commandsManager.worldCommandBuffer[threadId].push(
                    type = WorldCommandType.DECREMENT_DIVIDE_COUNTER,
                    ints = intArrayOf(organismIndex)
                )
            }

            if (action.physicalLink.isNotEmpty()) {
                val gridX = getX(index).toInt()
                val gridY = getY(index).toInt()
                val closestCells = gridManager.collectCells(gridX, gridY)
                val idToIndexAssociation =
                    closestCells.filter { organIndex[it] == organIndex[index] }
                        .associateBy { cellGenomeId[it] }

                action.physicalLink.forEach { (cellGenomeIdToConnectWith, linkData) ->
                    val otherCellIndex = idToIndexAssociation[cellGenomeIdToConnectWith]
                    if (linkData != null && otherCellIndex != null && linkData.length != null) {
                        if (linkData.isNeuronal && linkData.directedNeuronLink != action.id
                            && linkData.directedNeuronLink != cellGenomeIdToConnectWith
                        ) {
                            throw Exception("Incorrect logic in the neural-link")
                        }

                        val cellIndex: Int = index
                        val otherCellIndex: Int = otherCellIndex
                        val linksLength: Float = linkData.length
                        val degreeOfShortening: Float = 1f
                        val isStickyLink: Boolean = false
                        val isNeuronLink: Boolean = linkData.isNeuronal
                        val isLink1NeuralDirected: Boolean =
                            linkData.directedNeuronLink == action.id

                        commandsManager.worldCommandBuffer[threadId].push(
                            type = WorldCommandType.ADD_LINK,
                            booleans = booleanArrayOf(
                                isStickyLink,
                                isNeuronLink,
                                isLink1NeuralDirected
                            ),
                            floats = floatArrayOf(linksLength, degreeOfShortening),
                            ints = intArrayOf(cellIndex, otherCellIndex)
                        )
                    }
                }
            }

            if (parentIndex[index] == -1) {
                angleDiff[index] = angle[index] + PI.toFloat() - (action.angle ?: return)
            }

            energy[index] -= energyNecessaryToDivide[index]
        }
    }
}
