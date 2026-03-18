package io.github.some_example_name.old.systems.physics

import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.commands.WorldCommandType
import io.github.some_example_name.old.core.DIContainer.cellsSettings
import io.github.some_example_name.old.core.DIContainer.energyTransportRate
import io.github.some_example_name.old.core.DIContainer.linkMaxLength2
import io.github.some_example_name.old.core.SubstrateSettings
import io.github.some_example_name.old.core.utils.invSqrt
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.NeuralEntity
import io.github.some_example_name.old.entities.ParticleEntity

class LinkPhysicsSystem(
    val linkEntity: LinkEntity,
    val particleEntity: ParticleEntity,
    val substrateSettings: SubstrateSettings,
    val cellEntity: CellEntity,
    val commandsManager: CommandsManager
) {

    fun iterateLinks() {
        //TODO process parallel
        for (linkId in 0..linkEntity.lastId) {
            if (linkEntity.isAlive[linkId])
                processLink(linkId, threadId = 0)
        }
    }

    private fun transportEnergy(linkCell1: Int, linkCell2: Int) = with(cellEntity) {
        val cell1maxEnergy = cellsSettings[cellType[linkCell1].toInt()].maxEnergy
        val cell2maxEnergy = cellsSettings[cellType[linkCell2].toInt()].maxEnergy
        if (energy[linkCell1] / cell1maxEnergy < energy[linkCell2] / cell2maxEnergy) {
            energy[linkCell1] += energyTransportRate
            energy[linkCell2] -= energyTransportRate
        } else if (energy[linkCell1] / cell1maxEnergy != energy[linkCell2] / cell2maxEnergy) {
            energy[linkCell1] -= energyTransportRate
            energy[linkCell2] += energyTransportRate
        }
    }

    private fun transportNeuroSignal(linkId: Int, linkCell1: Int, linkCell2: Int) = with(cellEntity) {
        if (linkEntity.isNeuronLink[linkId]) {
            val signalToCellIndex = if (linkEntity.isLink1NeuralDirected[linkId]) linkCell1 else linkCell2
            val signalFromCellIndex = if (linkEntity.isLink1NeuralDirected[linkId]) linkCell2 else linkCell1
            val neuronImpulseInputSum = getNeuronImpulseInput(signalToCellIndex)
            if (getIsSum(signalToCellIndex)) {
                setNeuronImpulseInput(signalToCellIndex, neuronImpulseInputSum + getNeuronImpulseOutput(signalFromCellIndex))
            } else {
                setNeuronImpulseInput(signalToCellIndex, neuronImpulseInputSum * getNeuronImpulseOutput(signalFromCellIndex))
            }
        }
    }

    private fun processLink(linkIndex: Int, threadId: Int) = with(particleEntity) {
        with(linkEntity) {
            val linkCell1 = links1[linkIndex]
            val linkCell2 = links2[linkIndex]

            val dx = x[linkCell1] - x[linkCell2]
            val dy = y[linkCell1] - y[linkCell2]

//            transportEnergy(linkCell1, linkCell2)
//            transportNeuroSignal(linkId, linkCell1, linkCell2)

            val distanceSquared = dx * dx + dy * dy

            if (distanceSquared > linkMaxLength2) {
                commandsManager.worldCommandBuffer[threadId].push(
                    type = WorldCommandType.DELETE_LINK,
                    ints = intArrayOf(linkIndex)
                )
                return
            }
            // TODO: for physical accuracy this should be changed to a harmonic mean
            val stiffness = (cellStiffness[linkCell1] + cellStiffness[linkCell2]) / 2

            if (distanceSquared <= 0) throw Exception("distanceSquared <= 0, distanceSquared = $distanceSquared")
            val dist = 1.0f / invSqrt(distanceSquared)

            val force = (dist - linksNaturalLength[linkIndex] * degreeOfShortening[linkIndex]) * stiffness

            val dirX = dx / dist
            val dirY = dy / dist

            // Spring dampening
            val dvx = vx[linkCell1] - vx[linkCell2]
            val dvy = vy[linkCell1] - vy[linkCell2]

            val dampeningConstant = 0.3f
            val dampeningForce = dampeningConstant * (dvx * dirX + dvy * dirY)

            val fx = (force + dampeningForce) * dirX
            val fy = (force + dampeningForce) * dirY

            vx[linkCell2] += fx
            vy[linkCell2] += fy
            vx[linkCell1] -= fx
            vy[linkCell1] -= fy
        }
    }

}
