package io.github.some_example_name.old.cells.base

import io.github.some_example_name.old.cells.Cell

class CellBuilder {

    val instances = Cell::class.sealedSubclasses.mapNotNull { subclass ->
        subclass.constructors
            .firstOrNull { it.parameters.isEmpty() }
            ?.call()
    }.sortedBy { it.cellTypeId }

}

fun main() {

}
