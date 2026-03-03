package io.github.some_example_name.old.cells

import io.github.some_example_name.old.cells.base.activation
import io.github.some_example_name.old.core.utils.skyBlueColors

class Eye: Cell(
    defaultColor = skyBlueColors[2],
    cellTypeId = 14
) {

    override fun doOnTick(index: Int, threadId: Int) = with(cellEntity) {
        if (simEntity.tickCounter % 4 == 0) {

            neuronImpulseOutput[index] = activation(cellEntity, index, 0f)

            energy[index] -= substrateSettings.cellsSettings[cellType[index] + 1].energyActionCost
        }
    }
}
