package io.github.some_example_name.old.entities

class SubstancesEntity(
    startMaxAmount: Int = 5_000
): Entity(startMaxAmount) {
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
