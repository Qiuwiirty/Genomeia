package io.github.some_example_name.old.systems.genomics

import io.github.some_example_name.old.cells.base.activation
import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.commands.WorldCommandType
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.OrganEntity
import io.github.some_example_name.old.systems.genomics.genome.GenomeManager
import io.github.some_example_name.old.systems.genomics.genomic_transformations.divideCell
import io.github.some_example_name.old.systems.genomics.genomic_transformations.mutateCell
import kotlin.math.atan2

class CellManager(
    val cellEntity: CellEntity,
    val linkEntity: LinkEntity,
    val organEntity: OrganEntity,
    val genomeManager: GenomeManager,
    val commandsManager: CommandsManager,
) {

    fun killCell(cellIndex: Int, threadId: Int) {
        commandsManager.deletedCellSizes[threadId] += 1
        commandsManager.deleteCellLists[threadId][
            commandsManager.deletedCellSizes[threadId]
        ] = cellIndex

        val base = cellIndex * MAX_LINK_AMOUNT
        val amount = cellEntity.linksAmount[cellIndex]

        for (j in 0 until amount) {
            val idx = base + j
            val linkId = cellEntity.links[idx]
            commandsManager.addToDeleteList(threadId, linkId)
        }
    }

    //TODO Calculate the angle only relative to the parent cell, and only for the cells where it’s necessary, or only at the moment of division
    private fun processCellAngle(cellIndex: Int) {
        if (cellEntity.parentIndex[cellIndex] != -1) {
            val linkId = linkEntity.linkIndexMap.get(cellIndex, cellEntity.parentIndex[cellIndex])
            if (linkId == -1) return //TODO потетсить всякие варинаты без этой защиты
            val c1 = linkEntity.links1[linkId]
            val c2 = linkEntity.links2[linkId]
            val childCellIndex = if (cellIndex != c2) c2 else c1

            val dx = cellEntity.getX(cellIndex) - cellEntity.getX(childCellIndex)
            val dy = cellEntity.getY(cellIndex) - cellEntity.getY(childCellIndex)
            val angleToChild = atan2(dy, dx)

            cellEntity.angle[cellIndex] = angleToChild + cellEntity.angleDiff[cellIndex]
        }
    }

    fun processCell(
        cellIndex: Int,
        threadId: Int
    ) {
        if (!cellEntity.isAliveCell[cellIndex]) return
        if (cellEntity.energy[cellIndex] <= 0f) killCell(cellIndex, threadId)

        if (cellEntity.isNeuronTransportable[cellIndex]) {
            //TODO Add a flag to perform impulse calculations only for cells through which a neural impulse passes
            cellEntity.neuronImpulseOutput[cellIndex] = activation(cellEntity, cellIndex, cellEntity.neuronImpulseInput[cellIndex])
        }
        //TODO сделать вызовы для клеток
//        doSpecific(cellEntity.cellType[cellIndex], cellIndex, threadId) // This can override neuronImpulseOutput
        cellEntity.neuronImpulseInput[cellIndex] = if (cellEntity.isSum[cellIndex]) 0f else 1f

        genomicTransformations(cellIndex, threadId)
        processCellAngle(cellIndex)
    }

    fun genomicTransformations(cellIndex: Int, threadId: Int) {
        val organIndex = cellEntity.organismIndex[cellIndex]
        if (!organEntity.alreadyGrownUp[organIndex]) {
            if (organEntity.justChangedStage[organIndex]) {
                val currentStage = genomeManager.genomes[organEntity.genomeIndex[organIndex]]
                    .genomeStageInstruction[organEntity.stage[organIndex]]
                val action = currentStage.cellActions[cellEntity.cellGenomeId[cellIndex]]
                val isDivideNotNull = action?.divide != null
                val isMutateNotNull = action?.mutate != null

                cellEntity.cellActions[cellIndex] = action

                cellEntity.isDividedInThisStage[cellIndex] = !isDivideNotNull
                cellEntity.isMutateInThisStage[cellIndex] = !isMutateNotNull

                if (isDivideNotNull) {
                    //TODO Make a more accurate energy calculation
                    cellEntity.energyNecessaryToDivide[cellIndex] = 2.5f
                    commandsManager.worldCommandBuffer[threadId].push(
                        type = WorldCommandType.DIVIDE_ALIVE_CELL_ACTION_COUNTER,
                        intArrayOf(organIndex)
                    )
                }

                if (isMutateNotNull) {
                    //TODO Make a more accurate energy calculation
                    cellEntity.energyNecessaryToMutate[cellIndex] = 1.2f
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

    companion object{

        const val MAX_LINK_AMOUNT = 12
    }
}
