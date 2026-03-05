package io.github.some_example_name.old.entities

import java.util.TreeMap

class SimEntity: Entity {
    var isRestart = false
    var isFinish = false
    var tickCounter = 0
    var timeSimulation = 0f

    //Пока вынес все глобальные переменные сюда, но кажется это все не совсем к месту
    var currentGenomeIndex = 0


    var isPlay = true
    var maxSpeed = false
    var ups = 60
    var grabbedCell = -1

    //TODO подумать как сделать лучше
    val controllerIndexesLol = TreeMap<Int, Boolean>()

    override fun copy() {
        TODO("Not yet implemented")
    }

    override fun paste() {
        TODO("Not yet implemented")
    }

    override fun clear() {
        isRestart = false
        isFinish = false
        tickCounter = 0
        timeSimulation = 0f
        currentGenomeIndex = 0
    }

    override fun resize() {

    }
}
