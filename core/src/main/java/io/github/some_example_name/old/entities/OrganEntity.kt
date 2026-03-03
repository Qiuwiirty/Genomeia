package io.github.some_example_name.old.entities

import java.util.BitSet

class OrganEntity(
    organStartMaxAmount: Int
): Entity {

    private var organMaxAmount = organStartMaxAmount
    var organLastId = -1

    var genomeIndex = IntArray(organMaxAmount)
    var genomeSize = IntArray(organMaxAmount)
    var stage = IntArray(organMaxAmount)
    var dividedTimes = IntArray(organMaxAmount)
    var mutatedTimes = IntArray(organMaxAmount)
    var alreadyGrownUp = BitSet(organMaxAmount)
    var divideCounterThisStage = IntArray(organMaxAmount)
    var mutateCounterThisStage = IntArray(organMaxAmount)
    var divideAmountThisStage = IntArray(organMaxAmount)
    var mutateAmountThisStage = IntArray(organMaxAmount)
    var justChangedStage = BitSet(organMaxAmount)

    override fun copy() {
        TODO("Not yet implemented")
    }

    override fun paste() {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun resize() {
        TODO("Not yet implemented")
    }
}
