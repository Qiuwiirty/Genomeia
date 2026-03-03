package io.github.some_example_name.old.cells

import io.github.some_example_name.old.core.utils.redColors

class Punisher : Cell(
    defaultColor = redColors[0],
    cellTypeId = 24
) {

    override fun onContact(index: Int, indexCollided: Int, threadId: Int) = with(cellEntity) {
        if (organismIndex[index] != organismIndex[indexCollided] &&
            cellType[indexCollided] != -1 &&
            cellType[indexCollided] != 2 &&
            cellType[indexCollided] != 24) {
            val maxEnergy = substrateSettings.cellsSettings[cellType[index] + 1].maxEnergy
            if (energy[index] >= maxEnergy) {
                energy[index] -= maxEnergy
                //TODO command to delete cell
                //killCell(indexCollided, threadId)
            }
        }
    }
}
