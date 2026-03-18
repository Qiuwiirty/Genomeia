package io.github.some_example_name.old.entities

import java.util.BitSet

class OrganEntity(
    organStartMaxAmount: Int
): Entity(organStartMaxAmount) {

    var genomeIndex = IntArray(maxAmount)
    var genomeSize = IntArray(maxAmount)
    var stage = IntArray(maxAmount)
    var dividedTimes = IntArray(maxAmount)
    var mutatedTimes = IntArray(maxAmount)
    var alreadyGrownUp = BitSet(maxAmount)
    var divideCounterThisStage = IntArray(maxAmount)//Для подсчета убитых клеток
    var mutateCounterThisStage = IntArray(maxAmount)//Для подсчета убитых клеток
    var divideAmountThisStage = IntArray(maxAmount)//Общее количество
    var mutateAmountThisStage = IntArray(maxAmount)//Общее количество
    var justChangedStage = BitSet(maxAmount)

    override fun onCopy() {
        TODO("Not yet implemented")
    }

    override fun onPaste() {
        TODO("Not yet implemented")
    }

    override fun onClear(bound: Int) {
        TODO("Not yet implemented")
    }

    override fun onResize(oldMax: Int) {
        TODO("Not yet implemented")
    }
}
