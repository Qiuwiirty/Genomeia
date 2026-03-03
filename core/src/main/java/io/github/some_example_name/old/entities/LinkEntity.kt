package io.github.some_example_name.old.entities

import io.github.some_example_name.old.core.utils.UnorderedIntPairMap
import java.util.BitSet

class LinkEntity (
    linksStartMaxAmount: Int
): Entity{
    var linksMaxAmount = linksStartMaxAmount
    var linksLastId = -1
    var deadLinksStackAmount = -1
    var deadLinksStack = IntArray(250_000) { -1 }


    var isAliveLink = BitSet(linksMaxAmount)
    var linkGeneration = IntArray(linksMaxAmount)
    var links1 = IntArray(linksMaxAmount) { -1 }
    var links2 = IntArray(linksMaxAmount) { -1 }
    var linksNaturalLength = FloatArray(linksMaxAmount) { -10f }
    var isNeuronLink = BitSet(linksMaxAmount)
    var isLink1NeuralDirected = BitSet(linksMaxAmount)
    var degreeOfShortening = FloatArray(linksMaxAmount) { 1f }
    var isStickyLink = BooleanArray(linksMaxAmount) { false }
    val linkIndexMap = UnorderedIntPairMap(1_000_000)


    override fun copy() {
        TODO("Not yet implemented")
    }

    override fun paste() {
        TODO("Not yet implemented")
    }

    override fun clear() {
        val linkBound = (linksLastId + 1).coerceAtLeast(0)
        linksLastId = -1

        deadLinksStack.fill(-1, 0, (deadLinksStackAmount + 1).coerceAtLeast(0))
        deadLinksStackAmount = -1

        linkGeneration.fill(0, 0, linkBound)
        isAliveLink.clear()
        links1.fill(-1, 0, linkBound)
        links2.fill(-1, 0, linkBound)
        linksNaturalLength.fill(-10f, 0, linkBound)
        isNeuronLink.clear()
        isLink1NeuralDirected.clear()
        degreeOfShortening.fill(1f, 0, linkBound)
        isStickyLink.fill(false, 0, linkBound)
        linkIndexMap.clear()
    }

    override fun resize() {
        val oldMax = linksMaxAmount
        linksMaxAmount = (oldMax * 5 / 4).coerceAtLeast(oldMax + 1)

        // Resize link arrays
        run {
            val old = isAliveLink
            isAliveLink = BitSet(linksMaxAmount)
            isAliveLink.or(old)
        }
        run {
            val old = linkGeneration
            linkGeneration = IntArray(linksMaxAmount)
            System.arraycopy(old, 0, linkGeneration, 0, oldMax)
        }
        run {
            val old = links1
            links1 = IntArray(linksMaxAmount) { -1 }
            System.arraycopy(old, 0, links1, 0, oldMax)
        }
        run {
            val old = links2
            links2 = IntArray(linksMaxAmount) { -1 }
            System.arraycopy(old, 0, links2, 0, oldMax)
        }
        run {
            val old = linksNaturalLength
            linksNaturalLength = FloatArray(linksMaxAmount) { -10f }
            System.arraycopy(old, 0, linksNaturalLength, 0, oldMax)
        }
        run {
            val old = isNeuronLink
            isNeuronLink = BitSet(linksMaxAmount)
            isNeuronLink.or(old)
        }
        run {
            val old = isLink1NeuralDirected
            isLink1NeuralDirected = BitSet(linksMaxAmount)
            isLink1NeuralDirected.or(old)
        }
        run {
            val old = degreeOfShortening
            degreeOfShortening = FloatArray(linksMaxAmount) { 1f }
            System.arraycopy(old, 0, degreeOfShortening, 0, oldMax)
        }
        run {
            val old = isStickyLink
            isStickyLink = BooleanArray(linksMaxAmount) { false }
            System.arraycopy(old, 0, isStickyLink, 0, oldMax)
        }
    }
}
