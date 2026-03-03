package io.github.some_example_name.old.cells

import io.github.some_example_name.old.cells.base.activation
import io.github.some_example_name.old.core.utils.blueColors
import kotlin.math.*

class Compass : Cell(
    defaultColor = blueColors[6],
    cellTypeId = 15,
    isDirected = true
) {

    override fun doOnTick(index: Int, threadId: Int) = with(cellEntity) {
        // TODO: redesign to send a signal to the cell whose connection is closer to the center
        if (simEntity.tickCounter % 7 == 0) {
            val angleRad = angle[index]
            neuronImpulseOutput[index] = activation(cellEntity, index, sin(angleRad))

            energy[index] -= substrateSettings.cellsSettings[cellType[index] + 1].energyActionCost
        }
        cellEntity.energy[index] -= substrateSettings.cellsSettings[cellType[index] + 1].energyActionCost
    }

}
