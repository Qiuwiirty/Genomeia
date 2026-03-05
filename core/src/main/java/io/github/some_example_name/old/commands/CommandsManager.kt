package io.github.some_example_name.old.commands

import io.github.some_example_name.old.systems.genomics.genome.Action
import io.github.some_example_name.old.systems.simulation.ThreadManager.Companion.THREAD_COUNT

/*
* Команды которые поступают от Мира и Игрока, выполняются только в updateAfterCycle
* Commands that come from the World and the Player are executed only in updateAfterCycle
* */
class CommandsManager(
    threadCount: Int = THREAD_COUNT
) {


    val userCommandBuffer = UserCommandBuffer()
    val worldCommandBuffer = Array(THREAD_COUNT) { WorldCommandBuffer() }

    //A buffer for updating cell positions — maybe I’ll be able to come up with something better
    //TODO resize
    val evenChunkPositionStack = Array(THREAD_COUNT) { IntArray(30_000) }
    val oddChunkPositionStack = Array(THREAD_COUNT) { IntArray(30_000) }

    val evenCounter = IntArray(threadCount)
    val oddCounter = IntArray(threadCount)


    //TODO переделать в WorldCommandBuffer

    val deletedCellSizes = IntArray(threadCount) { -1 }
    val deleteCellLists = Array(threadCount) { IntArray(1301) { -1 } }
    val deletedLinkSizes = IntArray(threadCount) { -1 }
    val deleteLinkLists = Array(threadCount) { IntArray(1300) { -1 } }
    fun addToDeleteList(threadId: Int, linkId: Int) {
        deletedLinkSizes[threadId] += 1
        deleteLinkLists[threadId][deletedLinkSizes[threadId]] = linkId
    }

    val addCells = Array(threadCount) { mutableListOf<AddCell>() }
    val addLinks = Array(threadCount) { mutableListOf<AddLink>() }
//    val addSubstances = Array(threadCount) { mutableListOf<SubstanceAdd>() }

    val addOrganisms = Array(threadCount) { mutableListOf<Organism>() }

    val decrementMutationCounter = Array(threadCount) { mutableListOf<Int>() }

}


class AddCell(
    val action: Action,
    val parentX: Float,
    val parentY: Float,
    val parentAngle: Float,
    val parentId: Int,
    val parentOrganismId: Int,
    val parentIndex: Int
)

class AddLink(
    val cellIndex: Int,
    val otherCellIndex: Int,
    val linksLength: Float,
    val degreeOfShortening: Float,
    val isStickyLink: Boolean,
    val isNeuronLink: Boolean,
    val isLink1NeuralDirected: Boolean,
//    val directedNeuronLink: Int
)

//TODO 100% Implement it using a structure of arrays
class Organism(
    var genomeIndex: Int,
    var genomeSize: Int,
    var stage: Int = 0,
    var dividedTimes: Int = 0,
    var mutatedTimes: Int = 0,
    var alreadyGrownUp: Boolean = false,
    var divideCounterThisStage: Int = 0,
    var mutateCounterThisStage: Int = 0,
    var divideAmountThisStage: Int = 0,
    var mutateAmountThisStage: Int = 0,
    var justChangedStage: Boolean = true
) {
    override fun toString(): String {
        return "Organism(genomeIndex=$genomeIndex, genomeSize=$genomeSize, stage=$stage, dividedTimes=${dividedTimes}, mutatedTimes=${mutatedTimes}, justChangedStage=$justChangedStage)"
    }
}
