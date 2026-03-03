package io.github.some_example_name.old.systems.genomics

import io.github.some_example_name.old.entities.OrganEntity
import io.github.some_example_name.old.systems.genomics.genome.GenomeManager

class OrganManager(
    private val organEntity: OrganEntity,
    private val genomeManager: GenomeManager
) {

    /*
    * Переход на следющую стадию генома в каждом организме
    * Transition to the next stage of the genome in each organism
    * */
    fun performOrgansNextStage(): Boolean? {
        with(organEntity) {
            for (index in 0..organEntity.organLastId) {
                if (alreadyGrownUp[index]) return null
                justChangedStage[index] = false
                if (dividedTimes[index] == divideAmountThisStage[index] - divideCounterThisStage[index]
                    && mutatedTimes[index] == mutateAmountThisStage[index] - mutateCounterThisStage[index]
                ) {
                    if (genomeSize[index] > stage[index] + 1) {
                        stage[index]++
                        val currentGenome = genomeManager.genomes[genomeIndex[index]]
                        justChangedStage[index] = true
                        divideCounterThisStage[index] = 0
                        mutateCounterThisStage[index] = 0
                        divideAmountThisStage[index] = currentGenome.dividedTimes[stage[index]]
                        mutateAmountThisStage[index] = currentGenome.mutatedTimes[stage[index]]
                        dividedTimes[index] = currentGenome.dividedTimes[stage[index]]
                        mutatedTimes[index] = currentGenome.mutatedTimes[stage[index]]
                        return true
                    } else {
                        println("organism grown $index")
                        //TODO Delete grown organs
                        alreadyGrownUp[index] = true
                        return null
                    }
                }
            }
        }
        return false
    }

    fun clear() {
        organEntity.clear()
    }
}
