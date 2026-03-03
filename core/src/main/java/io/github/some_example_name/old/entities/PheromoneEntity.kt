package io.github.some_example_name.old.entities

import io.github.some_example_name.old.systems.physics.GridManager.Companion.GRID_SIZE

class PheromoneEntity: Entity {

    // Pheromone system
    // Could easily be expanded to support other substances dissolved in the substrate
    var pheromoneR = FloatArray(GRID_SIZE) { 0f }
    var pheromoneG = FloatArray(GRID_SIZE) { 0f }
    var pheromoneB = FloatArray(GRID_SIZE) { 0f }

    override fun copy() {

    }

    override fun paste() {

    }

    override fun clear() {
        pheromoneR.fill(0f, 0, GRID_SIZE)
        pheromoneG.fill(0f, 0, GRID_SIZE)
        pheromoneB.fill(0f, 0, GRID_SIZE)
    }

    override fun resize() {

    }
}
