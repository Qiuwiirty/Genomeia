package io.github.some_example_name.old.core

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Json
import io.github.some_example_name.old.commands.CommandsManager
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.OrganEntity
import io.github.some_example_name.old.entities.ParticleEntity
import io.github.some_example_name.old.entities.PheromoneEntity
import io.github.some_example_name.old.entities.SimEntity
import io.github.some_example_name.old.entities.SubstancesEntity
import io.github.some_example_name.old.systems.genomics.OrganManager
import io.github.some_example_name.old.systems.genomics.genome.GenomeManager
import io.github.some_example_name.old.systems.genomics.genome.json.GenomeJsonReader
import io.github.some_example_name.old.systems.physics.GridManager
import io.github.some_example_name.old.systems.physics.PhysicsSystem
import io.github.some_example_name.old.systems.render.RenderSystem
import io.github.some_example_name.old.systems.render.ShaderManager
import io.github.some_example_name.old.systems.render.ShaderManagerLibgdxApi
import io.github.some_example_name.old.systems.render.TripleBufferManager
import io.github.some_example_name.old.systems.simulation.SimulationSystem
import io.github.some_example_name.old.systems.simulation.ThreadManager
import java.util.Locale
import kotlin.getValue

object DIContainer {
    val json by lazy { Json() }
    val bundle: I18NBundle by lazy {
        I18NBundle.createBundle(
            Gdx.files.internal("ui/i18n/MyBundle"),
            Locale.getDefault()
        )
    }

    val gridManager = GridManager()
    val commandsManager = CommandsManager()
    val organEntity = OrganEntity(
        organStartMaxAmount = 400
    )
    val simEntity = SimEntity()
    val particleEntity = ParticleEntity(
        particlesStartMaxAmount = 12_000,
        gridManager = gridManager
    )
    val cellEntity = CellEntity(
        cellsStartMaxAmount = 10_000,
        particleEntity = particleEntity,
        simEntity = simEntity
    )
    val linkEntity = LinkEntity(
        20_000
    )
    val pheromoneEntity = PheromoneEntity()
    val substancesEntity = SubstancesEntity()
    val genomeJsonReader = GenomeJsonReader()
    val genomeManager = GenomeManager(
        genomeJsonReader = genomeJsonReader,
        simEntity = simEntity,
        isGenomeEditor = false,
        genomeName = "TODO"
    )
    val organManager = OrganManager(
        organEntity = organEntity,
        genomeManager = genomeManager
    )
    val substrateSettings = SubstrateSettings()
    val physicsSystem = PhysicsSystem(
        entity = particleEntity,
        gridManager = gridManager,
        substrateSettings = substrateSettings,
        commandsManager = commandsManager
    )
    val threadManager = ThreadManager(
        simEntity = simEntity
    )

    val tripleBufferManager = TripleBufferManager(
        particleEntity
    )

    val shaderManager: ShaderManager = when (Gdx.app.type) {
        Application.ApplicationType.Desktop -> ShaderManagerLibgdxApi()
        Application.ApplicationType.Android -> TODO()
        Application.ApplicationType.HeadlessDesktop -> TODO()
        Application.ApplicationType.Applet -> TODO()
        Application.ApplicationType.WebGL -> TODO()
        Application.ApplicationType.iOS -> TODO()
    }

    val renderSystem = RenderSystem(
        tripleBufferManager = tripleBufferManager,
        simEntity = simEntity,
        cellEntity = cellEntity,
        linkEntity = linkEntity,
        shaderManager = shaderManager,
        particleEntity = particleEntity
    )

    val simulationSystem by lazy {
        SimulationSystem(
            gridManager = gridManager,
            commandsManager = commandsManager,
            organManager = organManager,
            organEntity = organEntity,
            cellEntity = cellEntity,
            linkEntity = linkEntity,
            particleEntity = particleEntity,
            pheromoneEntity = pheromoneEntity,
            substancesEntity = substancesEntity,
            substrateSettings = substrateSettings,
            threadManager = threadManager,
            genomeManager = genomeManager,
            physicsSystem = physicsSystem,
            simEntity = simEntity,
            tripleBufferManager = tripleBufferManager
        )
    }
}
