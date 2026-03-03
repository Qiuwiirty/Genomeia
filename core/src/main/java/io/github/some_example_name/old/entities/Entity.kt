package io.github.some_example_name.old.entities

sealed interface Entity {
    fun copy()
    fun paste()
    fun clear()
    fun resize()
}
