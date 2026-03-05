package io.github.some_example_name.old.good_one

import kotlin.math.*
import java.util.*

// Assuming osl.types equivalents
typealias Frac = Double
data class Vec2(var x: Frac, var y: Frac) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Frac) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Frac) = Vec2(x / scalar, y / scalar)
    operator fun unaryMinus() = Vec2(-x, -y)
    fun length(): Frac = sqrt(x * x + y * y)
    fun dot(other: Vec2): Frac = x * other.x + y * other.y
}

data class FVec4(var r: Frac, var g: Frac, var b: Frac, var a: Frac)
data class FVec3(var x: Frac, var y: Frac, var z: Frac)

// Placeholder for random, assuming a Random instance
val rand = Random()

// Placeholder for HSV2RGB, implement if needed
fun HSV2RGB(hsv: FVec3): FVec3 {
    // Simple placeholder implementation (actual HSV to RGB conversion)
    return FVec3(hsv.x, hsv.y, hsv.z) // Replace with real conversion
}

// Placeholder for benchmarks, dtm, swap_delta, ups_limiter, etc.
// For understanding, we can use println or simple maps
val bench = mutableMapOf<String, MutableList<Double>>()

// Placeholder classes
class Timer { fun get(): Double = 0.0 } // Mock
val dtm = Timer()
val swap_delta = Timer()
class Limiter { fun set(v: Double) {} fun sync(b: Boolean, d: Double): Boolean = true }
val ups_limiter = Limiter()

// Assuming WorldAdapter and related
class WorldAdapter {
    data class WorldSettingsT(var cellsLimit: Int, var worldSize: Frac)
    val wkvResponseQueuePtr: Queue<Any> = LinkedList() // Placeholder
    val isRunning = true // AtomicBoolean equivalent, but simple Boolean for now
    val criticalWait: () -> Unit = {} // Placeholder
    val wkvPullCommands: MutableList<Any> = mutableListOf() // Placeholder
}

// Cell type enum
class CellT {
    enum class TypeT { NONE, PHAGO }
    var type: TypeT = TypeT.NONE
    var pos: Vec2 = Vec2(0.0, 0.0)
    var color: FVec4 = FVec4(0.0, 0.0, 0.0, 0.0)
    var velocity: Vec2 = Vec2(0.0, 0.0)
    var impulse: Vec2 = Vec2(0.0, 0.0)
    var radius: Frac = 0.0
    var weight: Frac = 0.0
    var forcePredict: Vec2 = Vec2(0.0, 0.0)
    var force: Vec2 = Vec2(0.0, 0.0)
    var forveAbs: Frac = 0.0
    var velocityPredict: Vec2 = Vec2(0.0, 0.0)
}

// Cells storage
class CellsPC {
    val storage: MutableList<CellT> = mutableListOf()
    val enabled: MutableList<Int> = mutableListOf() // IDs
}

// Line struct
data class LineStructT(var index: Int = 0, var x: Frac = 0.0, var y: Frac = 0.0, var yFloor: Frac = 0.0)

// World class
class World {
    var cellsLimit: Int = 0
    val wa = WorldAdapter()
    val lines: MutableList<LineStructT> = mutableListOf()
    val cellsPC = CellsPC()
    val detectedPairCollisionVec: MutableList<Pair<Int, Int>> = mutableListOf()
    var isPaused: Boolean = false
    var worldStepCounter: Long = 0
    var collisionCounter: Int = 0
    var checkCounter: Int = 0

    fun enableNewCell(): Int {
        val id = cellsPC.storage.size
        cellsPC.storage.add(CellT())
        cellsPC.enabled.add(id)
        return id
    }

    // sync function (empty as per code)
    fun sync(isMainSync: Boolean) {
        // Implementation if needed
    }

    fun run(ws: WorldAdapter.WorldSettingsT) {
        cellsLimit = ws.cellsLimit
        // Allocate video memory before simulation
        wa.wkvPullCommands.add("reallock_tb" to cellsLimit.toLong())
        wa.wkvResponseQueuePtr.add(wa.wkvPullCommands) // Push to queue
        // Reserve own memory
        lines.reserveCapacity(cellsLimit + 2)
        cellsPC.storage.reserveCapacity(cellsLimit)
        detectedPairCollisionVec.reserveCapacity(cellsLimit * 5)
        wa.criticalWait() // Wait for host to process request
        if (!wa.isRunning) return
        swap_delta.get()
        ups_limiter.set(20.0)
        // Initialization
        run {
            val cells = cellsPC.storage
            // Reserve memory
            // Create cells
            for (i in 0 until cellsLimit) {
                val c = cells[enableNewCell()]
                // Initial initialization
                c.type = CellT.TypeT.PHAGO
                c.pos = Vec2(rand.nextDouble(), rand.nextDouble()) * (ws.worldSize * 0.5)
                val hsv = FVec3(rand.nextDouble().toFloat().toDouble(), 1.0 - (rand.nextDouble().pow(4.0)), 1.0 - 0.7 * rand.nextDouble().pow(2.0))
                c.color = FVec4(HSV2RGB(hsv).x.toFloat().toDouble(), HSV2RGB(hsv).y.toFloat().toDouble(), HSV2RGB(hsv).z.toFloat().toDouble(), 1.0)
            }
            // Initialize physical parameters of cells
            for (id in cellsPC.enabled) {
                val c = cells[id]
                c.velocity = Vec2(0.0, 0.0)
                c.impulse = Vec2(0.0, 0.0)
                c.radius = 0.4 + rand.nextDouble() * 0.1
                c.weight = c.radius * c.radius
            }
        }
        // Main simulation loop
        while (wa.isRunning) {
            dtm.get() // Reset timer
            worldStepCounter++
            if (!isPaused) {
                // 1. Prepare data for collision search
                physics1()
                bench.getOrPut("GaP_1") { mutableListOf() }.add(dtm.get())
                // 2. Search for collisions
                physics2()
                bench.getOrPut("GaP_2") { mutableListOf() }.add(dtm.get())
                // 3. Process collisions
                physics3()
                bench.getOrPut("collision") { mutableListOf() }.add(dtm.get())
                // 4. Update cell states
                updateCells()
                bench.getOrPut("update_cells") { mutableListOf() }.add(dtm.get())
            }
            // 4. Synchronization and graphics data preparation
            while (ups_limiter.sync(true, 100.0)) {
                sync(false) // Intermediate sync for user events
            }
            dtm.get() // Account for full sync time
            sync(true) // Full sync with graphics prep
            bench.getOrPut("sync") { mutableListOf() }.add(dtm.get())
        }
        println("World was stop")
    }

    // Placeholder for update_cells
    fun updateCells() {
        // Implementation as needed
    }

    // Compute pair force
    fun computePairForce(deltaPos: Vec2, r: Frac, v: Frac): Vec2 {
        val k1: Frac = 0.2 // Damping coefficient on approach
        val k2: Frac = 1.0 // Damping coefficient on separation
        val aFactor: Frac = 1.0 // Transition width
        val vFactor = if (v < 0) k1 * v else if (v < aFactor) k1 * v + ((k2 - k1) / (2 * aFactor)) * v * v else k2 * v - ((k2 - k1) * aFactor / 2)
        val k = -(if (vFactor > 0.9) 0.9 else if (vFactor < -0.3) -0.3 else vFactor)
        val nfv = deltaPos / r
        val f = (1.0 - (r * (1.0 - k) / (1.0 - k * (r * 2.0 - 1.0)))) * 12.0
        val fv = nfv * f
        return fv
    }

    // Process collision
    inline fun <reified T : Boolean> processCollision(a: CellT, b: CellT, predictive: T): Boolean {
        val deltaPos = a.pos - b.pos
        val sumR = a.radius + b.radius
        val sqrR = deltaPos.dot(deltaPos)
        if (predictive == true as T) { // For predictive (first pass)
            if (sqrR > sumR * sumR || a.pos == b.pos) return false
        }
        val relV = if (predictive == true as T) a.velocity - b.velocity else (((a.velocityPredict - b.velocityPredict) + (a.velocity - b.velocity)) * 0.5)
        val dist = sqrt(sqrR)
        val n = deltaPos / dist
        val vApproach = relV.dot(n) / sumR
        val normR = dist / sumR
        val fv = computePairForce(deltaPos, normR, vApproach)
        if (predictive == true as T) {
            a.forcePredict += fv
            b.forcePredict -= fv
        } else {
            // a.forveAbs += fv.length()
            // b.forveAbs += fv.length()
            a.force += fv
            b.force -= fv
        }
        return true
    }

    // Physics stage 1: Prepare data and sort
    fun physics1() {
        // Copy positions to lines array (skip inactive cells)
        val writeIt = mutableListOf<LineStructT>()
        for (line in lines) {
            val cell = cellsPC.storage[line.index]
            if (cell.type != CellT.TypeT.NONE) {
                val w = LineStructT(line.index, floor(cell.pos.y), cell.pos.x)
                w.yFloor = w.x // Swapped for sorting
                writeIt.add(w)
            }
        }
        lines.clear()
        lines.addAll(writeIt)
        // Sort lines (lexicographic by floor(Y), then X, then id)
        lines.sortWith(compareBy({ it.yFloor }, { it.x }, { it.index })) // Assuming yFloor is floor(Y), x is pos.x
        // Restore positions for stage 2
        for (c in lines) {
            val cellPos = cellsPC.storage[c.index].pos
            c.yFloor = c.x // In x was floor(pos.y)
            c.x = cellPos.x
            c.y = cellPos.y
        }
        // Add two dummy elements for edge cases
        lines.add(LineStructT())
        lines.add(LineStructT())
    }

    // Physics stage 2: Collision detection
    fun physics2() {
        val cellsNum = cellsPC.enabled.size
        val cells = cellsPC.storage
        detectedPairCollisionVec.clear()
        val maxDiameter = 1.0
        var c3ItIndex = 0
        for (c1ItIndex in 0 until lines.size - 1) {
            val c1 = lines[c1ItIndex]
            val stopXRight = c1.x + maxDiameter
            val stopY = c1.yFloor + 1.0
            val subStopXLeft = c1.x - maxDiameter
            val subStopXRight = c1.x + maxDiameter
            val subStopY = c1.yFloor + 1.0
            // Adjust c3_it
            while (lines[c3ItIndex].y < subStopY) c3ItIndex++
            while (lines[c3ItIndex].yFloor == lines.getOrNull(c3ItIndex + 1)?.yFloor && lines[c3ItIndex].x < subStopXLeft) c3ItIndex++
            // On current line
            var c2ItIndex = c1ItIndex + 1
            while (c2ItIndex < lines.size && lines[c2ItIndex].x < stopXRight && lines[c2ItIndex].y < stopY) {
                detectedPairCollisionVec.add(c1.index to lines[c2ItIndex].index)
                c2ItIndex++
            }
            // On line above
            c2ItIndex = c3ItIndex
            while (c2ItIndex < lines.size && lines[c2ItIndex].x < subStopXRight && lines[c2ItIndex].y < stopY + 1.0) {
                detectedPairCollisionVec.add(c1.index to lines[c2ItIndex].index)
                c2ItIndex++
            }
        }
        // Remove dummy elements
        lines.removeAt(lines.lastIndex)
        lines.removeAt(lines.lastIndex)
    }

    // Physics stage 3: Process all detected collisions
    fun physics3() {
        // Reset force sums
        for (cid in cellsPC.enabled) {
            val cell = cellsPC.storage[cid]
            cell.forcePredict = Vec2(0.0, 0.0)
            cell.force = Vec2(0.0, 0.0)
            cell.forveAbs = 0.0
        }
        // Counter for valid pairs
        var collisionCounter = 0
        // Compute force sums for all detected collisions (predictive)
        val newVec = mutableListOf<Pair<Int, Int>>()
        for ((aId, bId) in detectedPairCollisionVec) {
            if (processCollision(cellsPC.storage[aId], cellsPC.storage[bId], true)) {
                newVec.add(aId to bId)
                collisionCounter++
            }
        }
        detectedPairCollisionVec.clear()
        detectedPairCollisionVec.addAll(newVec)
        // Record performance counters
        this.collisionCounter = collisionCounter
        checkCounter = detectedPairCollisionVec.size
        val dev = maxOf(cellsPC.enabled.size.toDouble(), 1.0)
        bench.getOrPut("GaP_cc") { mutableListOf() }.add(checkCounter.toDouble() / dev)
        bench.getOrPut("avr_collision") { mutableListOf() }.add(collisionCounter.toDouble() / dev)
        val substeps: Frac = 20.0
        val deltaTime = 1.0 / substeps
        val viscosity = 0.01
        for (cid in cellsPC.enabled) {
            val cell = cellsPC.storage[cid]
            cell.velocityPredict = cell.velocity + (cell.forcePredict / cell.weight * deltaTime * 0.5)
        }
        // Second pass with velocity correction
        for ((aId, bId) in detectedPairCollisionVec) {
            processCollision(cellsPC.storage[aId], cellsPC.storage[bId], false)
        }
        for (cid in cellsPC.enabled) {
            val cell = cellsPC.storage[cid]
            // Dynamic viscosity friction
            cell.force -= cell.velocity.times(viscosity)
            // Pull to center for debugging
            cell.force = mix(cell.force, -cell.pos, 0.001)
            // Compute acceleration: a = F / m
            val acceleration = cell.force / cell.weight
            // Update velocity: v += a * dt
            cell.velocity += acceleration * deltaTime
            // Update position: pos += v * dt
            cell.pos += cell.velocity * deltaTime
            // Update impulse: p = m * v
            cell.impulse = cell.velocity.times(cell.weight)
        }
    }

    // Helper mix function (like osl::mix)
    fun mix(a: Vec2, b: Vec2, t: Frac): Vec2 = a + (b - a) * t
}

// Extensions for Random
fun Random.nf(): Frac = nextDouble()
fun Random.pf(): Frac = nextDouble()
fun Random.pd(): Frac = nextDouble()

// Reserve capacity extension (for lists)
fun <T> MutableList<T>.reserveCapacity(cap: Int) {
    // Kotlin lists don't have reserve, but we can ignore or use ensureCapacity if ArrayList
    if (this is ArrayList) ensureCapacity(cap)
}
