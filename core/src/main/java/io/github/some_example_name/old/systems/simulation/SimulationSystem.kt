package io.github.some_example_name.old.systems.simulation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.commands.PlayerCommand
import io.github.some_example_name.old.commands.WorldCommandType
import io.github.some_example_name.old.core.DIContainer.threadCount
import io.github.some_example_name.old.core.SubstrateSettings
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.OrganEntity
import io.github.some_example_name.old.entities.ParticleEntity
import io.github.some_example_name.old.entities.PheromoneEntity
import io.github.some_example_name.old.entities.SimEntity
import io.github.some_example_name.old.entities.SubstancesEntity
import io.github.some_example_name.old.systems.genomics.CellManager
import io.github.some_example_name.old.systems.genomics.OrganManager
import io.github.some_example_name.old.systems.genomics.genome.GenomeManager
import io.github.some_example_name.old.systems.physics.GridManager
import io.github.some_example_name.old.systems.physics.LinkPhysicsSystem
import io.github.some_example_name.old.systems.physics.ParticlePhysicsSystem
import io.github.some_example_name.old.systems.render.TripleBufferManager
import kotlin.collections.get
import kotlin.inc
import kotlin.math.sqrt
import kotlin.random.Random

class SimulationSystem(
    val gridManager: GridManager,
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
    val particlePhysicsSystem: ParticlePhysicsSystem,
    val linkPhysicsSystem: LinkPhysicsSystem,
    val simEntity: SimEntity,
    val tripleBufferManager: TripleBufferManager,
    val cellManager: CellManager
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

        executingCommandsFromTheWorld()
        processParticleCollision()
        linkPhysicsSystem.iterateLinks()
        cellManager.iterateCell()
        //TODO Process substance

        arrangementOfPositionsInTheGrid()
        tripleBufferManager.updateAndCommitProducer()
        organManager.performOrgansNextStage()
        processingCommandsFromUser()
    }

    fun processParticleCollision() {
        threadManager.runChunkStage(isOdd = true) { start, end, threadId ->
            particlePhysicsSystem.processGridChunkPhysics(start, end, threadId, isOdd = true)
        }
        threadManager.runChunkStage(isOdd = false) { start, end, threadId ->
            particlePhysicsSystem.processGridChunkPhysics(start, end, threadId, isOdd = false)
        }
    }

    fun arrangementOfPositionsInTheGrid() {
        for (chunk in 0..<threadCount) {
            threadManager.futures.add(threadManager.executor.submit {
                for (i in 0..<commandsManager.oddCounter[chunk]) {
                    particlePhysicsSystem.moveParticle(commandsManager.oddChunkPositionStack[chunk][i])
                }
            })
        }
        threadManager.futures.forEach { it.get() }
        threadManager.futures.clear()

        for (chunk in 0..<threadCount) {
            threadManager.futures.add(threadManager.executor.submit {
                for (i in 0..<commandsManager.evenCounter[chunk]) {
                    particlePhysicsSystem.moveParticle(commandsManager.evenChunkPositionStack[chunk][i])
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
                        organEntity.divideCounterThisStage[ints[0]]++
                    }
                    WorldCommandType.MUTATE_ALIVE_CELL_ACTION_COUNTER -> {
                        organEntity.mutateCounterThisStage[ints[0]]++
                    }
                    WorldCommandType.ADD_PARTICLE -> {
                        particleEntity.addParticle(
                            x = floats[0],
                            y = floats[1],
                            radius = floats[2],
                            color = ints[0]
                        )
                    }
                    WorldCommandType.ADD_LINK -> {
                        linkEntity.addLink(
                            cellIndex = ints[0],
                            otherCellIndex = ints[1],
                            linksLength = floats[0],
                            degreeOfShortening = floats[1],
                            isStickyLink = booleans[0],
                            isNeuronLink = booleans[1],
                            isLink1NeuralDirected = booleans[2]
                        )
                    }
                    WorldCommandType.DELETE_LINK -> {
                        linkEntity.deleteLink(linkIndex = ints[0])
                    }
                    WorldCommandType.ADD_CELL -> {
                        cellEntity.addCell(
                            x = floats[0],
                            y = floats[1],
                            color = ints[0],
                            radius = floats[2],
                            cellGenomeId = ints[1],
                            cellType = ints[2],
                            organismIndex = ints[3],
                            parentIndex = ints[4],
                            angle = floats[3],
                            angleDiff = floats[4],
                            colorDifferentiation = ints[5],
                            visibilityRange = floats[5],
                            a = floats[6],
                            b = floats[7],
                            c = floats[8],
                            isSum = booleans[0],
                            activationFuncType = ints[6].toByte()
                        )
                    }
                    WorldCommandType.DECREMENT_DIVIDE_COUNTER -> {
                        organEntity.dividedTimes[ints[0]]--
                    }
                    WorldCommandType.DECREMENT_MUTATION_COUNTER -> {
                        organEntity.mutatedTimes[ints[0]]--
                    }
                    WorldCommandType.DELETE_CELL -> {
                        val cellIndex = ints[0]
                        organManager.cellDeleted(cellIndex)
                        cellEntity.deleteCell(cellIndex)
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
                    val radius = 10.0f

                    repeat(300/*0_000*/) {
                        val angle = MathUtils.random(0f, MathUtils.PI2)

                        // больше частиц ближе к центру
                        val r = radius * sqrt(MathUtils.random())

                        val x = cmd.x + MathUtils.cos(angle) * r
                        val y = cmd.y + MathUtils.sin(angle) * r

                        if (x > 0 && x < gridManager.gridWidth && y > 0 && y < gridManager.gridHeight) {
                            val r = 128 + Random.nextInt(128)
                            val g = 128 + Random.nextInt(128)
                            val b = 128 + Random.nextInt(128)
                            val a = 255

                            val color = (r shl 24) or (g shl 16) or (b shl 8) or a

                            commandsManager.worldCommandBuffer[0].push(
                                type = WorldCommandType.ADD_PARTICLE,
                                floats = floatArrayOf(
                                    x,
                                    y,
                                    MathUtils.random(0.1f, 0.5f)
                                ),
                                ints = intArrayOf(color)
                            )
                        }
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

    fun performWorldCommands() = with(commandsManager) {

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
