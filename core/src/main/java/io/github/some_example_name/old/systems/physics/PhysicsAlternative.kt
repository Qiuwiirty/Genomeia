package io.github.some_example_name.old.systems.physics

import io.github.some_example_name.old.entities.ParticleEntity
import kotlin.math.floor


class PhysicsAlternative(
    val entity: ParticleEntity
) {
    val size = 100_000
    // Lines
    var index = IntArray(size)
    var x = FloatArray(size)
    var y = FloatArray(size)
    var yFloor = FloatArray(size)
    var indicesSorted: MutableList<Int> = mutableListOf() // Use mutable list to allow appending dummies

    var detectCounter = 0
    var detectedCollisionA = IntArray(200_000)
    var detectedCollisionB = IntArray(200_000)

    var lineCounter = 0

    fun prepareAndSort() {
        lineCounter = 0
//        entity.aliveParticlesIndex.forEach { it ->
        for(i in 0.. entity.particleLastId) {
            index[lineCounter] = i
            x[lineCounter] = entity.x[i]
            y[lineCounter] = entity.y[i]
            yFloor[lineCounter] = floor(entity.y[i])
            lineCounter++
        }

        indicesSorted.clear()
        repeat(lineCounter) { indicesSorted.add(it) }

        // Sort lines (lexicographic by floor(Y), then X, then id)
        indicesSorted.sortWith(compareBy(
            { yFloor[it] },
            { x[it] },
            { index[it] }
        ))

        repeat(2) {
            index[lineCounter] = -1 // Invalid ID to avoid bad pairs
            x[lineCounter] = Float.MAX_VALUE // Sentinel values to stop advances safely
            y[lineCounter] = Float.MAX_VALUE
            yFloor[lineCounter] = Float.MAX_VALUE
            indicesSorted.add(lineCounter) // Append dummy index
            lineCounter++
        }
    }

    fun detectCollision() {
        detectCounter = 0
        var c3ItIndex = 0
        for (c1ItIndex in 0 until lineCounter - 1) {
            val lineIndexC1 = indicesSorted[c1ItIndex]
            val stopXRight = x[lineIndexC1] + PARTICLE_MAX_DIAMETER
            val stopY = yFloor[lineIndexC1] + 1.0f // Use float for consistency
            val subStopXLeft = x[lineIndexC1] - PARTICLE_MAX_DIAMETER
            val subStopXRight = x[lineIndexC1] + PARTICLE_MAX_DIAMETER
            val subStopY = yFloor[lineIndexC1] + 1.0f

            // Adjust c3_it
            while (y[indicesSorted[c3ItIndex]] < subStopY) c3ItIndex++
            while (c3ItIndex + 1 < lineCounter && // Extra bounds check for safety
                yFloor[indicesSorted[c3ItIndex]] == yFloor[indicesSorted[c3ItIndex + 1]] &&
                x[indicesSorted[c3ItIndex]] < subStopXLeft) c3ItIndex++

            // On current line
            var c2ItIndex = c1ItIndex + 1
            while (c2ItIndex < lineCounter &&
                x[indicesSorted[c2ItIndex]] < stopXRight &&
                y[indicesSorted[c2ItIndex]] < stopY) {
                val a = index[lineIndexC1]
                val b = index[indicesSorted[c2ItIndex]]
                if (a >= 0 && b >= 0) { // Skip invalid dummy IDs
                    detectedCollisionA[detectCounter] = a
                    detectedCollisionB[detectCounter] = b
                    detectCounter++
                }
                c2ItIndex++
            }

            // On line above
            c2ItIndex = c3ItIndex
            while (c2ItIndex < lineCounter &&
                x[indicesSorted[c2ItIndex]] < subStopXRight &&
                y[indicesSorted[c2ItIndex]] < stopY + 1.0f) {
                val a = index[lineIndexC1]
                val b = index[indicesSorted[c2ItIndex]]
                if (a >= 0 && b >= 0) {
                    detectedCollisionA[detectCounter] = a
                    detectedCollisionB[detectCounter] = b
                    detectCounter++
                }
                c2ItIndex++
            }
            // Removed detectCounter -= 2 (bug)
        }
    }

    companion object {
        const val PARTICLE_MAX_DIAMETER = 1.0f
    }
}
