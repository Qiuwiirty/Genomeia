package io.github.some_example_name.old.cells

import io.github.some_example_name.old.commands.Organism
import io.github.some_example_name.old.core.utils.pinkColors

class Zygote: Cell(
    defaultColor = pinkColors[0],
    cellTypeId = 18
) {

    override fun onStart(index: Int, parentGenomeIndex: Int, threadId: Int) {
        with(cellEntity) {
//        if (!cm.isGenomeEditor) {
//            cm.cellGenomeId[id] = 0
//        }
            organIndex[index] = organEntity.lastId + 1
            val currentGenome = genomeManager.genomes[parentGenomeIndex]

            val newOrganism = Organism(
                genomeIndex = parentGenomeIndex,
                genomeSize = currentGenome.genomeStageInstruction.size,
                stage = 0,
                dividedTimes = currentGenome.dividedTimes[0],
                mutatedTimes = currentGenome.mutatedTimes[0],
                justChangedStage = true,
                alreadyGrownUp = false,
                divideAmountThisStage = currentGenome.dividedTimes[0],
                mutateAmountThisStage = currentGenome.mutatedTimes[0]
            )
//            TODO add organ command
//            if (threadId != -1) {
//                cm.addOrganisms[threadId].add(newOrganism)
//            } else {
//                cm.organismManager.organisms.add(newOrganism)
//            }
        }
    }
}
