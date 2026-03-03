package io.github.some_example_name.old.cells

import io.github.some_example_name.old.cells.base.activation
import io.github.some_example_name.old.core.utils.pinkColors
import io.github.some_example_name.old.systems.simulation.SimulationSystem
import kotlin.collections.get

class Sticky: Cell(
    defaultColor = pinkColors[3],
    cellTypeId = 11
) {

    override fun onContact(index: Int, indexCollided: Int, threadId: Int) = with(cellEntity) {
        if (cellType[index] == 11 && activation(cellEntity, index, neuronImpulseInput[index]) < 1f) {
//            addStickyLink(id, collidedId, distance, threadId)
//            addLinks[threadId].add(
//                AddLink(
//                    cellIndex = cellId,
//                    otherCellIndex = otherCellId,
//                    linksLength = distance,
//                    degreeOfShortening = 1f,
//                    isStickyLink = true,
//                    isNeuronLink = false,
//                    isLink1NeuralDirected = false
//                )
//            )
            commandsManager.addLinks
            return
        }
    }
}
