package io.github.some_example_name.old.entities

import io.github.some_example_name.old.systems.physics.GridManager
import io.github.some_example_name.old.systems.physics.PhysicsSystem.Companion.PARTICLE_MAX_RADIUS
import it.unimi.dsi.fastutil.ints.IntArrayList
import java.util.BitSet
import kotlin.collections.fill
import kotlin.math.PI

class ParticleEntity(
    particlesStartMaxAmount: Int,
    val gridManager: GridManager
): Entity {
    var particleMaxAmount = particlesStartMaxAmount
    var particleLastId = -1

    var deadParticlesStackAmount = -1
    var deadParticlesStack = IntArray(250_000) { -1 }
    val aliveParticlesIndex = IntArrayList()

    var isAliveParticle = BitSet(particleMaxAmount)

    var gridId = IntArray(particleMaxAmount) { -1 }
    var x = FloatArray(particleMaxAmount)
    var y = FloatArray(particleMaxAmount)
    var vx = FloatArray(particleMaxAmount)
    var vy = FloatArray(particleMaxAmount)
    var radius = FloatArray(particleMaxAmount) { PARTICLE_MAX_RADIUS }
    var mass = FloatArray(particleMaxAmount)
    var color = IntArray(particleMaxAmount)
    var dragCoefficient = FloatArray(particleMaxAmount) { 0.003f }
    var effectOnContact = BitSet(particleMaxAmount)
    var cellStiffness = FloatArray(particleMaxAmount) { 0.5f }

    fun addParticle(
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
        vx: Float = 0f,
        vy: Float = 0f,
        dragCoefficient: Float = 0.03f,
        effectOnContact: Boolean = false,
        cellStiffness: Float = 0.02f
    ) {
        val index =
            if (deadParticlesStackAmount >= 0) deadParticlesStack[deadParticlesStackAmount--]
            else ++particleLastId
        isAliveParticle[index] = true

        gridId[index] = gridManager.addParticle(x.toInt(), y.toInt(), index)

        this.x[index] = x
        this.y[index] = y
        this.radius[index] = radius
        this.color[index] = color
        this.cellStiffness[index] = cellStiffness
        this.dragCoefficient[index] = dragCoefficient
        this.mass[index] = radius * radius * PI.toFloat()
    }

    override fun copy() {

    }

    override fun paste() {

    }

    override fun clear() {
        val particleBound = (particleLastId + 1).coerceAtLeast(0)

        particleLastId = -1

        deadParticlesStack.fill(-1, 0, (deadParticlesStackAmount + 1).coerceAtLeast(0))
        deadParticlesStackAmount = -1

        gridId.fill(-1, 0, particleBound)
        isAliveParticle.clear()
        x.fill(0f, 0, particleBound)
        y.fill(0f, 0, particleBound)
        vx.fill(0f, 0, particleBound)
        vy.fill(0f, 0, particleBound)
        radius.fill(PARTICLE_MAX_RADIUS, 0, particleBound)
        color.fill(0, 0, particleBound)
        mass.fill(0f, 0, particleBound)
        dragCoefficient.fill(0.03f, 0, particleBound)
        effectOnContact.clear()
        cellStiffness.fill(0f, 0, particleBound)
    }

    override fun resize() {
        val oldMax = particleMaxAmount
        particleMaxAmount = (oldMax * 5 / 4).coerceAtLeast(oldMax + 1)

        run {
            val old = isAliveParticle
            isAliveParticle = BitSet(particleMaxAmount)
            isAliveParticle.or(old)
        }
        run {
            val old = gridId
            gridId = IntArray(particleMaxAmount) { -1 }
            System.arraycopy(old, 0, gridId, 0, oldMax)
        }
        run {
            val old = x
            x = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, x, 0, oldMax)
        }
        run {
            val old = y
            y = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, y, 0, oldMax)
        }
        run {
            val old = effectOnContact
            effectOnContact = BitSet(particleMaxAmount)
            effectOnContact.or(old)
        }
        run {
            val old = vx
            vx = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, vx, 0, oldMax)
        }
        run {
            val old = vy
            vy = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, vy, 0, oldMax)
        }
        run {
            val old = dragCoefficient
            dragCoefficient = FloatArray(particleMaxAmount) { 0.03f }
            System.arraycopy(old, 0, dragCoefficient, 0, oldMax)
        }
        run {
            val old = cellStiffness
            cellStiffness = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, cellStiffness, 0, oldMax)
        }
        run {
            val old = color
            color = IntArray(particleMaxAmount)
            System.arraycopy(old, 0, color, 0, oldMax)
        }
        run {
            val old = mass
            mass = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, mass, 0, oldMax)
        }
        run {
            val old = radius
            radius = FloatArray(particleMaxAmount)
            System.arraycopy(old, 0, radius, 0, oldMax)
        }
    }
}
