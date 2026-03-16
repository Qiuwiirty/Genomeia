package io.github.some_example_name.old.systems.physics

import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.core.DIContainer.halfChunkHeight
import io.github.some_example_name.old.core.SubstrateSettings
import io.github.some_example_name.old.entities.ParticleEntity
import io.github.some_example_name.old.core.utils.invSqrt
import io.github.some_example_name.old.entities.SimEntity
import kotlin.math.sqrt

class PhysicsSystem(
    val entity: ParticleEntity,
    val gridManager: GridManager,
    val substrateSettings: SubstrateSettings,
    val commandsManager: CommandsManager,
    val simEntity: SimEntity
) {

    val halfChunkHeight2 = halfChunkHeight * halfChunkHeight

    fun processGridChunkPhysics(start: Int, end: Int, threadId: Int, isOdd: Boolean) {
        for (i in start until end) {
            val x = i % gridManager.gridWidth
            val y = i / gridManager.gridWidth

            if (gridManager.particleCounts[i] > 0) {
                val particles = gridManager.getParticlesIndex(i)
                processCollisionsInTheSameCell(particles, threadId)
                for (particleIndex in particles) {
                    processNeighborsCellsCollision(particleIndex, x, y, threadId)
                    distributeParticleIndicesAcrossChunks(particleIndex, threadId, isOdd)
                }
            }
        }
    }

    fun distributeParticleIndicesAcrossChunks(
        cellIndex: Int,
        threadId: Int,
        isOdd: Boolean
    ) {
        if (isOdd) {
            commandsManager.oddChunkPositionStack[threadId][commandsManager.oddCounter[threadId]] = cellIndex
            commandsManager.oddCounter[threadId]++
        } else {
            commandsManager.evenChunkPositionStack[threadId][commandsManager.evenCounter[threadId]] = cellIndex
            commandsManager.evenCounter[threadId]++
        }
    }

    private fun processNeighborsCellsCollision(cellId: Int, gridX: Int, gridY: Int, threadId: Int) {
        gridManager.getParticles(gridX - 1, gridY + 1).also { ids ->
            for (id in ids) repulse(cellId, id, false, threadId)
        }
        gridManager.getParticles(gridX, gridY + 1).also { ids ->
            for (id in ids) repulse(cellId, id, false, threadId)
        }
        gridManager.getParticles(gridX + 1, gridY + 1).also { ids ->
            for (id in ids) repulse(cellId, id, false, threadId)
        }

        gridManager.getParticles(gridX + 1, gridY).also { ids ->
            for (id in ids) repulse(cellId, id, false, threadId)
        }
    }

    private fun processCollisionsInTheSameCell(cells: IntArray, threadId: Int) {
        for (i in cells.indices) {
            for (j in i + 1 until cells.size) {
                repulse(cells[i], cells[j], true, threadId)
            }
        }
    }

    private fun repulse(cellAId: Int, cellBId: Int, isSameCell: Boolean = false, threadId: Int) = with(entity) {

//        val linkId = linkIndexMap.get(cellAId, cellBId)
//        if (isSameCell/* && linkId != -1*/) {
//            processLink(linkId, threadId)
//            return
//        }
//        if (linkId != -1) return

        if (cellBId < 0) println("cellBId")
        if (cellAId < 0) println("cellAId")
        val dx = x[cellAId] - x[cellBId]
        val dy = y[cellAId] - y[cellBId]
        val dx2 = dx * dx
        if (dx2 > MAX_RADIUS_SQUARED) return
        val dy2 = dy * dy
        if (dy2 > MAX_RADIUS_SQUARED) return

        val particleRadius = radius[cellAId] + radius[cellBId]
        val radiusSquared = particleRadius * particleRadius

        val distanceSquared = dx2 + dy2
        if (distanceSquared < radiusSquared) {
            val distance = 1.0f / invSqrt(distanceSquared)
            if (distance.isNaN()) throw Exception("TODO потом убрать")
/*

            if (effectOnContact[cellAId] || effectOnContact[cellBId]) {
                //TODO сделать буфер
//                if (cellType[cellBId] == 11) {
//                    Sticky.specificToThisType(this, cellBId, cellAId, threadId, distance)
//                } else if (cellType[cellAId] == 11) {
//                    Sticky.specificToThisType(this, cellAId, cellBId, threadId, distance)
//                }
//
//                if (cellType[cellBId] == 12) {
//                    Pumper.specificToThisType(this, cellBId, cellAId)
//                }
//                if (cellType[cellAId] == 12) {
//                    Pumper.specificToThisType(this, cellAId, cellBId)
//                }
//
//                if (cellType[cellBId] == 24) {
//                    Punisher.specificToThisType(this, cellBId, cellAId, threadId)
//                }
//                if (cellType[cellAId] == 24) {
//                    Punisher.specificToThisType(this, cellAId, cellBId, threadId)
//                }
            }
*/

//            val massA = mass[cellAId]
//            val massB = mass[cellBId]

            // Квадратичная зависимость силы
            val cellStrengthAverage = (cellStiffness[cellAId] + cellStiffness[cellBId]) / 2f
            val force = cellStrengthAverage - cellStrengthAverage * distanceSquared / radiusSquared
            // Нормализация вектора расстояния
            val normX = dx / distance
            val normY = dy / distance
            val vectorX = normX * force
            val vectorY = normY * force

            vx[cellAId] += vectorX /// massA
            vy[cellAId] += vectorY /// massA
            vx[cellBId] -= vectorX /// massB
            vy[cellBId] -= vectorY /// massB
        }
    }

/*

    private fun stretchLinks(cellId: Int, threadId: Int) = with(entity) {
        val base = cellId * MAX_LINK_AMOUNT
        val amount = linksAmount[cellId]
        if (amount == 0) return

        for (i in 0 until amount) {
            val idx = base + i
            val linkId = links[idx]
            val c1 = links1[linkId]
            val c2 = links2[linkId]
            val otherCellId = if (c1 != cellId) c1 else if (c2 != cellId) c2 else continue
            if (gridId[cellId] < gridId[otherCellId]) {
                processLink(linkId, threadId)
            }
        }
    }

    private fun processLink(linkId: Int, threadId: Int) = with(entity) {
        val linkCell1 = links1[linkId]
        val linkCell2 = links2[linkId]

        if (cellType[linkCell2] == -1 && cellType[linkCell1] == -1) return
        val dx = x[linkCell1] - x[linkCell2]
        val dy = y[linkCell1] - y[linkCell2]

        //transport energy
        val energyTransportRate = substrateSettings.data.rateOfEnergyTransferInLinks // made this a variable instead of a magic number

        val cell1maxEnergy = substrateSettings.cellsSettings[cellType[linkCell1] + 1].maxEnergy
        val cell2maxEnergy = substrateSettings.cellsSettings[cellType[linkCell2] + 1].maxEnergy

        if (energy[linkCell1] / cell1maxEnergy < energy[linkCell2] / cell2maxEnergy) {
            energy[linkCell1] += energyTransportRate
            energy[linkCell2] -= energyTransportRate
        } else if (energy[linkCell1] / cell1maxEnergy != energy[linkCell2] / cell2maxEnergy) {
            energy[linkCell1] -= energyTransportRate
            energy[linkCell2] += energyTransportRate
        }

        if (isNeuronLink[linkId]) {
            val signalToCellIndex = if (isLink1NeuralDirected[linkId]) linkCell1 else linkCell2
            val signalFromCellIndex = if (isLink1NeuralDirected[linkId]) linkCell2 else linkCell1
            if (isSum[signalToCellIndex]) {
                neuronImpulseInput[signalToCellIndex] += neuronImpulseOutput[signalFromCellIndex]
            } else {
                neuronImpulseInput[signalToCellIndex] *= neuronImpulseOutput[signalFromCellIndex]
            }
        }

        if (isStickyLink[linkId] && !isNeuronLink[linkId]) {
            if (cellType[linkCell1] == 11 && activation(
                    this,
                    linkCell2,
                    neuronImpulseOutput[linkCell2]
                ) >= 1
            ) {
                addToDeleteList(threadId, linkId)
            } else if (cellType[linkCell2] == 11 && activation(
                    this,
                    linkCell2,
                    neuronImpulseOutput[linkCell2]
                ) >= 1
            ) {
                addToDeleteList(threadId, linkId)
            }
        }

        val distanceSquared = dx * dx + dy * dy
        //TODO 25_600 move to substrate settings
        if (distanceSquared > 25_600) {
            addToDeleteList(threadId, linkId)
            return
        }
        // TODO: for physical accuracy this should be changed to a harmonic mean
        val stiffness =
            (cellsSettings[cellType[linkCell1] + 1].linkStiffness + cellsSettings[cellType[linkCell2] + 1].linkStiffness) / 2
        if (distanceSquared <= 0) return
        val dist = 1.0f / invSqrt(distanceSquared)

        val force = (dist - linksNaturalLength[linkId] * degreeOfShortening[linkId]) * stiffness

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
*/

    private fun processWorldBorders(cellId: Int) = with(entity) {
        if (x[cellId] < radius[cellId]) {
            x[cellId] = radius[cellId]
            vx[cellId] *= -0.8f
        } else if (x[cellId] > gridManager.gridWidth - radius[cellId]) {
            x[cellId] = gridManager.gridWidth - radius[cellId]
            vx[cellId] *= -0.8f
        }

        if (y[cellId] < radius[cellId]) {
            y[cellId] = radius[cellId]
            vy[cellId] *= -0.8f
        } else if (y[cellId] > gridManager.gridHeight - radius[cellId]) {
            y[cellId] = gridManager.gridHeight - radius[cellId]
            vy[cellId] *= -0.8f
        }
    }

    fun moveParticle(particleIndex: Int) = with(entity) {
        val oldX = x[particleIndex].toInt()
        val oldY = y[particleIndex].toInt()

        processCellFrictionOld(particleIndex)

        val vxv = vx[particleIndex]
        val vyv = vy[particleIndex]

        val speed2 = vxv * vxv + vyv * vyv
        if (speed2 > halfChunkHeight2) {
            val invLen = halfChunkHeight / sqrt(speed2)
            vx[particleIndex] *= invLen
            vy[particleIndex] *= invLen
        }

        x[particleIndex] += vx[particleIndex]
        y[particleIndex] += vy[particleIndex]

        processWorldBorders(particleIndex)
        val newX = x[particleIndex].toInt()
        val newY = y[particleIndex].toInt()
        if (newX != oldX || newY != oldY) {
            gridManager.removeParticle(oldX, oldY, particleIndex)
            gridId[particleIndex] = gridManager.addParticle(newX, newY, particleIndex)
        }
    }

    private fun processCellFrictionOld(cellId: Int) = with(entity) {
        vx[cellId] *= 1f - dragCoefficient[cellId]
        vy[cellId] *= 1f - dragCoefficient[cellId]
    }
/*
    private fun processCellFrictionSimpler(cellId: Int) = with(entity) {
        if (cellType[cellId] == 10) {
            vx[cellId] *= 1f - dragCoefficient[cellId]
            vy[cellId] *= 1f - dragCoefficient[cellId]
        } else {
            // Much more simplified version of the hydrodynamic drag equation
            // Drag is inversely proportional to the number of links a cell has
            // So cells on the surface of the organism have less drag than cells on the inside
            val amount = linksAmount[cellId] + 1
            vx[cellId] *= 1f - (1f - dragCoefficient[cellId]) / amount
            vy[cellId] *= 1f - (1f - dragCoefficient[cellId]) / amount
            //TODO move 0.005f to substrate settings
            vy[cellId] -= 0.005f
        }
    }*/
/*

    private fun processCellFriction(cellId: Int) = with(entity) {
        // Approximate hydrodynamic drag using surface normal calculated from neighboring cells
        // TODO: Use collision neighbors instead of linked neighbors
        // TODO: Fix phantom forces
        val base = cellId * MAX_LINK_AMOUNT
        val amount = linksAmount[cellId]
        if (amount == 0) {
            vx[cellId] *= dragCoefficient[cellId]
            vy[cellId] *= dragCoefficient[cellId]
            return
        }
        // Initialise the sum totals
        var vectorSumX = 0f
        var vectorSumY = 0f
        for (i in 0 until amount) {
            val idx = base + i
            val linkId = links[idx]
            val c1 = links1[linkId]
            val c2 = links2[linkId]
            val otherCellId = if (c1 != cellId) c1 else if (c2 != cellId) c2 else continue
            vectorSumX -= x[otherCellId]
            vectorSumY -= y[otherCellId]
        }
        // Normalise
        vectorSumX /= amount
        vectorSumY /= amount
        // Make relative to the cell
        vectorSumX += x[cellId]
        vectorSumY += y[cellId]
        // The friction proportional to alignment with the surface normal
        val frictionMultiplier = max(vx[cellId] * vectorSumX + vy[cellId] * vectorSumY, 0f)
        // Alternatively, we could project the friction force onto the surface normal like a more accurate hydrofoil

        vx[cellId] *= 1f - (1f - dragCoefficient[cellId]) * frictionMultiplier * 0.05f
        vy[cellId] *= 1f - (1f - dragCoefficient[cellId]) * frictionMultiplier * 0.05f
    }
*/

    companion object {
        const val PARTICLE_MAX_RADIUS = 0.5f
        const val MAX_RADIUS_SQUARED = 1600
    }
}
