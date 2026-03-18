package io.github.some_example_name.old.cells

import io.github.some_example_name.old.cells.base.activation
import io.github.some_example_name.old.core.utils.skyBlueColors

class Controller: Cell(
    defaultColor = skyBlueColors.last(),
    cellTypeId = 16,
    isNeural = true
) {
    //TODO PC: WASD ↑←↓→ 1234567890 (SPACE)
    //TODO smartphone: ↑←↓→ XYAB

    override fun doOnTick(index: Int, threadId: Int) = with(cellEntity) {
        //TODO сделать назначение клавиш на пк и добавление кнопок на телефонах
        val keyIndex = simEntity.controllerIndexesLol[cellGenomeId[index]] ?: return
        if (keyIndex) {
            setNeuronImpulseOutput(index, activation(index, 1f))
        } else {
            setNeuronImpulseOutput(index, 0f)
        }
        energy[index] -= substrateSettings.cellsSettings[cellType[index].toInt()].energyActionCost
    }
}
