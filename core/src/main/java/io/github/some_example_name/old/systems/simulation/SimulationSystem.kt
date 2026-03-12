package io.github.some_example_name.old.systems.simulation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.commands.PlayerCommand
import io.github.some_example_name.old.commands.WorldCommandType
import io.github.some_example_name.old.core.SubstrateSettings
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.OrganEntity
import io.github.some_example_name.old.entities.ParticleEntity
import io.github.some_example_name.old.entities.PheromoneEntity
import io.github.some_example_name.old.entities.SimEntity
import io.github.some_example_name.old.entities.SubstancesEntity
import io.github.some_example_name.old.systems.genomics.OrganManager
import io.github.some_example_name.old.systems.genomics.genome.GenomeManager
import io.github.some_example_name.old.systems.physics.GridManager
import io.github.some_example_name.old.systems.physics.GridManager.Companion.WORLD_SIZE_TYPE
import io.github.some_example_name.old.systems.physics.PhysicsSystem
import io.github.some_example_name.old.systems.render.TripleBufferManager
import kotlin.inc
import kotlin.math.sqrt
import kotlin.random.Random

class SimulationSystem(
    val gridManager: GridManager = GridManager(),
    val commandsManager: CommandsManager,
    val organManager: OrganManager,
    val organEntity: OrganEntity,
    val cellEntity: CellEntity,
    val linkEntity: LinkEntity,
    val particleEntity: ParticleEntity,
    val pheromoneEntity: PheromoneEntity,
    val substancesEntity: SubstancesEntity,
    val substrateSettings: SubstrateSettings,
    val threadManager: ThreadManager,
    val genomeManager: GenomeManager,
    val physicsSystem: PhysicsSystem,
    val simEntity: SimEntity,
    val tripleBufferManager: TripleBufferManager
) {

    val simulationThread = Thread { threadManager.runUpdateLoop { updateTick() } }

    fun startThread() {
        if (!threadManager.isRunning) {
            threadManager.isRunning = true
            simulationThread.start()
        }
    }

    fun updateTick() {
        if (simEntity.isFinish) {
            dispose()
        }
        if (simEntity.isRestart) {
            restartSim()
        }

        simEntity.tickCounter++
        simEntity.timeSimulation += DELTA_SIM_TICK_TIME

        processParticleCollision()
        //TODO Process link physics
        //TODO Process cell
        //TODO Process substance
        arrangementOfPositionsInTheGrid()

        tripleBufferManager.updateAndCommitProducer()

        organManager.performOrgansNextStage()
        executingCommandsFromTheWorld()
        processingCommandsFromUser()
    }

    fun processParticleCollision() {
        threadManager.runChunkStage(isOdd = true) { start, end, threadId ->
            physicsSystem.processGridChunkPhysics(start, end, threadId, isOdd = true)
        }
        threadManager.runChunkStage(isOdd = false) { start, end, threadId ->
            physicsSystem.processGridChunkPhysics(start, end, threadId, isOdd = false)
        }
    }

    fun arrangementOfPositionsInTheGrid() {
        for (chunk in 0..<ThreadManager.Companion.THREAD_COUNT) {
            threadManager.futures.add(threadManager.executor.submit {
                for (i in 0..<commandsManager.oddCounter[chunk]) {
                    physicsSystem.moveParticle(commandsManager.oddChunkPositionStack[chunk][i], chunk)
                }
            })
        }
        threadManager.futures.forEach { it.get() }
        threadManager.futures.clear()

        for (chunk in 0..<ThreadManager.Companion.THREAD_COUNT) {
            threadManager.futures.add(threadManager.executor.submit {
                for (i in 0..<commandsManager.evenCounter[chunk]) {
                    physicsSystem.moveParticle(commandsManager.evenChunkPositionStack[chunk][i], chunk)
                }
            })
        }
        threadManager.futures.forEach { it.get() }
        threadManager.futures.clear()

        commandsManager.oddCounter.fill(0)
        commandsManager.evenCounter.fill(0)
    }

    fun executingCommandsFromTheWorld() {
        commandsManager.worldCommandBuffer.forEach { worldCommandBuffer ->
            worldCommandBuffer.consume { type, ints, floats, booleans ->
                when (type) {
                    WorldCommandType.DIVIDE_ALIVE_CELL_ACTION_COUNTER -> {
                        val organismIndex = ints[0]
                        organEntity.divideCounterThisStage[organismIndex]++
                    }
                    WorldCommandType.MUTATE_ALIVE_CELL_ACTION_COUNTER -> {
                        val organismIndex = ints[0]
                        organEntity.mutateCounterThisStage[organismIndex]++
                    }
                    WorldCommandType.ADD_PARTICLE -> {
                        val x = floats[0]
                        val y = floats[1]
//                        val color = ints[0]
                        if (x > 0 && x < WORLD_SIZE_TYPE.size && y > 0 && y < WORLD_SIZE_TYPE.size) {
                            val radius = floats[2]

                            val r = 128 + Random.nextInt(128)
                            val g = 128 + Random.nextInt(128)
                            val b = 128 + Random.nextInt(128)
                            val a = 255

                            val color = (r shl 24) or (g shl 16) or (b shl 8) or a

                            particleEntity.addParticle(
                                x = x,
                                y = y,
                                radius = radius,
                                color = color//Color.rgba8888(Color.OLIVE)
                            )

                            if (particleEntity.particleMaxAmount - 2 < particleEntity.particleLastId) {
                                particleEntity.resize()
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun processingCommandsFromUser() {
        commandsManager.userCommandBuffer.swapAndConsume { cmd ->
            when (cmd) {
                is PlayerCommand.SpawnCell -> {
//                    repeat(10_000) {
//                        commandsManager.worldCommandBuffer[0].push(
//                            type = WorldCommandType.ADD_PARTICLE,
//                            floats = floatArrayOf(cmd.x + MathUtils.random(-40f, 40f), cmd.y + MathUtils.random(-40f, 40f), MathUtils.random(0.4f, 0.5f)),
//                            ints = intArrayOf(Color.rgba8888(Color.FOREST))
//                        )
//                    }
                    val radius = 20f

                    repeat(1_000) {
                        val angle = MathUtils.random(0f, MathUtils.PI2)

                        // больше частиц ближе к центру
                        val r = radius * sqrt(MathUtils.random())

                        val x = cmd.x + MathUtils.cos(angle) * r
                        val y = cmd.y + MathUtils.sin(angle) * r

                        commandsManager.worldCommandBuffer[0].push(
                            type = WorldCommandType.ADD_PARTICLE,
                            floats = floatArrayOf(
                                x,
                                y,
                                MathUtils.random(0.4f, 0.5f)
                            ),
                            ints = intArrayOf(Color.rgba8888(Color.FOREST))
                        )
                    }
//                    addCell(cmd.x, cmd.y, 18, false, genomeIndex = simEntity.currentGenomeIndex)
                }

                is PlayerCommand.DragCell -> {
//                    TODO()
//                    grabbedXLocal = cmd.dx
//                    grabbedYLocal = cmd.dy
//                    grabbedCellLocal = cmd.cellId
                }

                else -> {

                }
            }
        }
    }

    fun stopUpdateThread() {
        simulationThread.interrupt()
        try {
            simulationThread.join(1000) // ждём до 1 секунды // wait up to 1 second
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        threadManager.dispose()
    }

    fun dispose() {
        gridManager.clearAll()
        cellEntity.clear()
        linkEntity.clear()
        organEntity.clear()
        particleEntity.clear()
        pheromoneEntity.clear()
        substancesEntity.clear()
        simEntity.clear()
        organManager.clear()
    }

    private fun restartSim() {
        dispose()
        simEntity.isRestart = false
    }

    companion object {
        const val DELTA_SIM_TICK_TIME = 0.016666666f
    }
}

/*

    fun updateAfterCycle() {

        if (cellEntity.cellMaxAmount * 0.8 < cellEntity.cellLastId) {
            cellEntity.resize()
        }

        if (linkEntity.linksMaxAmount * 0.8 < linkEntity.linksLastId) {
            linkEntity.resize()
        }

        if (particleEntity.particleMaxAmount * 0.8 < particleEntity.particleLastId) {
            particleEntity.resize()
        }
    }

    fun performWorldCommands() = with(commandsManager) {
        /*
        * Добавление связей
        * Adding connections
        * */
        addLinks.forEach {
            it.forEach { link ->

                val addLinkId =
                    if (deadLinksStackAmount >= 0) deadLinksStack[deadLinksStackAmount--]
                    else ++linksLastId

                isAliveLink[addLinkId] = true
                linkGeneration[addLinkId]++

                linksNaturalLength[addLinkId] = link.linksLength
                degreeOfShortening[addLinkId] = link.degreeOfShortening
                isStickyLink[addLinkId] = link.isStickyLink
                isNeuronLink[addLinkId] = link.isNeuronLink
                isLink1NeuralDirected[addLinkId] = link.isLink1NeuralDirected

                links1[addLinkId] = link.cellIndex
                links2[addLinkId] = link.otherCellIndex
                linkIndexMap.put(link.cellIndex, link.otherCellIndex, addLinkId)
                addLink(link.cellIndex, addLinkId)
                addLink(link.otherCellIndex, addLinkId)
            }
            it.clear()
        }

        /*
        * Добавление клеток
        * Adding cells
        * */
        addCells.forEach {
            it.forEach { addCell ->
//                val organism = organismManager.organisms[addCell.parentOrganismId]
//                organism.dividedTimes--
                organEntity.dividedTimes[addCell.parentOrganismId]--
                addCell(addCell)
            }
            it.clear()
        }

        /*
        * Удаление клеток и связок
        * Removal of cells and ligaments
        * */

        for (i in 0 until threadCount) {
            val deleteLink = deleteLinkLists[i]
            for (j in 0..deletedLinkSizes[i]) {
                val deleteLinkIndex = deleteLink[j]
//                if (grabbedCellLocal == deleteLinkIndex) {
//                    grabbedCellLocal = -1
//                }
//                if (grabbedCell == deleteLinkIndex) {
//                    grabbedCell = -1
//                }
                deleteLink(deleteLinkIndex)
            }
            deletedLinkSizes[i] = -1
        }


        for (i in 0 until threadCount) {
            val deleteCell = deleteCellLists[i]
            for (j in 0..deletedCellSizes[i]) {
                val deleteCellIndex = deleteCell[j]
                deleteCell(deleteCellIndex)
            }
            deletedCellSizes[i] = -1
        }

        /*
        * Добавление организмов из мутаций Zygote
        * Adding organisms from Zygote mutations
        * */
        addOrganisms.forEach {
            it.forEach { addOrganism ->
                organismManager.organisms.add(addOrganism)
            }
            it.clear()
        }

        /*
        * Инкремент счетчика стадий для мутаиций
        * Increment the stage counter for mutations
        * */
        decrementMutationCounter.forEach {
            it.forEach { organismIndex ->
                organEntity.mutatedTimes[organismIndex]--
            }
            it.clear()
        }
    }
* */
