package io.github.some_example_name.old.good_one

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.some_example_name.old.ui.screens.GlobalSettings.UI_SCALE
import io.github.some_example_name.old.core.utils.drawArrowWithRotationAngle
import io.github.some_example_name.old.systems.simulation.SimulationSystem
import kotlin.math.round
/*

//TODO хз что за класс, рефаткорить
class PlayGround(
//    private val genomeEditor: GenomeEditorRefactored,
    private val spriteBatch: SpriteBatch,
    private val font: BitmapFont,
    private val simulationSystem: SimulationSystem
) {

    var zoomOffsetX = 0f
    var zoomOffsetY = 0f
    var isDragged = false

    var drawRays = false

    fun screenToWorld(screenX: Float, screenY: Float): Pair<Float, Float> {
        val scale = simulationSystem.zoomManager.zoomScale * simulationSystem.zoomManager.shaderCellSize
        val worldX = (screenX / scale) + simulationSystem.zoomManager.screenOffsetX
        val worldY =
            ((Gdx.graphics.height - screenY) / scale) + simulationSystem.zoomManager.screenOffsetY // Инверсия Y
        return worldX to worldY
    }

    fun screenToWorldPC(screenX: Float, screenY: Float): Pair<Float, Float> {
        val worldX =
            (screenX / (simulationSystem.zoomManager.zoomScale * simulationSystem.zoomManager.shaderCellSize)) + simulationSystem.zoomManager.screenOffsetX
        val worldY =
            (screenY / (simulationSystem.zoomManager.zoomScale * simulationSystem.zoomManager.shaderCellSize)) + simulationSystem.zoomManager.screenOffsetY
        return worldX to worldY
    }

    fun handlePlay() {

        for (i in 0 until 9) {
            val key = Input.Keys.NUM_1 + i
            val isPressed = Gdx.input.isKeyPressed(key)

            if (isPressed && !previousKeyStates[i]) {
                val entry = controllerIndexesLol.entries.elementAtOrNull(i)
                if (entry != null) {
                    controllerIndexesLol[entry.key] = true
                }
                keyStates[i] = true // Устанавливаем в true, когда клавиша нажата
            }
            if (!isPressed && previousKeyStates[i]) {
                val entry = controllerIndexesLol.entries.elementAtOrNull(i)
                if (entry != null) {
                    controllerIndexesLol[entry.key] = false
                }
                keyStates[i] = false // Устанавливаем в false, когда клавиша отпущена
            }

            // Обновляем предыдущее состояние
            previousKeyStates[i] = isPressed
        }

//        if (editButtonClicked && isEditPossible) {
//            editButtonClicked = false
//            if (cellManager.cellLastId != -1) {
//                genomeEditor.startEditing()
//                isPlay = false
//            }
//        }
        val updateTimeRounded = round(1e5f/ups) /100f
        if (simulationSystem.grabbedCell != -1) {
            spriteBatch.begin()
            font.data.setScale(UI_SCALE)
            font.draw(
                spriteBatch,
                "FPS: ${Gdx.graphics.framesPerSecond}\n" + "UPS: ${ups}\n" + "Update Time: ${updateTimeRounded}ms\n" + "Cells: ${simulationSystem.cellLastId*/
/* - 1535*//*
 - simulationSystem.deadCellsStackAmount} Links ${simulationSystem.linksLastId - simulationSystem.deadLinksStackAmount}\n" + "NeuronImpulseInput ${simulationSystem.neuronImpulseInput[simulationSystem.grabbedCell]}\n" + "NeuronImpulseOutput ${simulationSystem.neuronImpulseOutput[simulationSystem.grabbedCell]}\n",
                30f,
                120f
            )
            font.data.setScale(1f)
            spriteBatch.end()
        } else {
            spriteBatch.begin()
            font.data.setScale(UI_SCALE)
            font.draw(
                spriteBatch,
                "FPS: ${Gdx.graphics.framesPerSecond}\n"  + "UPS: ${ups}\n" + "Update Time: ${updateTimeRounded}ms\n" + "Cells: ${simulationSystem.cellLastId*/
/* - 1535*//*
 - simulationSystem.deadCellsStackAmount} Links ${simulationSystem.linksLastId - simulationSystem.deadLinksStackAmount}\n",
                30f,
                120f
            )
            font.data.setScale(1f)
            spriteBatch.end()
        }
    }

    fun update(renderer: ShapeRenderer) {
        val xOffset = simulationSystem.zoomManager.screenOffsetX
        val yOffset = simulationSystem.zoomManager.screenOffsetY
        val zoom = simulationSystem.zoomManager.zoomScale * simulationSystem.zoomManager.shaderCellSize
        val cameraEndX = simulationSystem.zoomManager.screenOffsetX + Gdx.graphics.width / zoom
        val cameraEndY = simulationSystem.zoomManager.screenOffsetY + Gdx.graphics.height / zoom


        renderer.begin(ShapeRenderer.ShapeType.Filled)
        renderer.color = Color(1.0f, 1f, 1f, 1f)

        for (i in 0..<simulationSystem.subManager.cellLastId + 1) {
            if (xOffset > simulationSystem.subManager.x[i]) continue
            if (yOffset > simulationSystem.subManager.y[i]) continue
            if (cameraEndX < simulationSystem.subManager.x[i]) continue
            if (cameraEndY < simulationSystem.subManager.y[i]) continue
            if (simulationSystem.subManager.x[i] > 0 && simulationSystem.subManager.y[i] > 0) {
                renderer.circle(
                    (simulationSystem.subManager.x[i] - xOffset) * zoom,
                    (simulationSystem.subManager.y[i] - yOffset) * zoom,
                    simulationSystem.subManager.radius[i] * zoom
                )
            }
        }
        renderer.end()
        Gdx.gl.glLineWidth(2f)


        renderer.begin(ShapeRenderer.ShapeType.Line)

//        synchronized(cellManager) {
//            for (i in 0..cellManager.linksLastId) {
//                if (cellManager.isAliveLink[i]) {
//                    renderer.color = if (cellManager.isNeuronLink[i]) Color.CYAN else Color.OLIVE
//                    if (!cellManager.isAliveCell[cellManager.links1[i]]) {
//                        renderer.color = Color.RED
//                    }
//                    if (!cellManager.isAliveCell[cellManager.links2[i]]) {
//                        renderer.color = Color.RED
//                    }
//                    val signalToCellIndex =
//                        if (cellManager.isLink1NeuralDirected[i]) cellManager.links1[i] else cellManager.links2[i]
//                    val signalFromCellIndex =
//                        if (cellManager.isLink1NeuralDirected[i]) cellManager.links2[i] else cellManager.links1[i]
//
//                    if (cellManager.isNeuronLink[i]) {
//                        renderer.drawTriangleMiddle(
//                            (cellManager.x[signalFromCellIndex] - xOffset) * zoom,
//                            (cellManager.y[signalFromCellIndex] - yOffset) * zoom,
//                            (cellManager.x[signalToCellIndex] - xOffset) * zoom,
//                            (cellManager.y[signalToCellIndex] - yOffset) * zoom,
//                            5f
//                        )
//                    }
//
//                    renderer.line(
//                        (cellManager.x[cellManager.links1[i]] - xOffset) * zoom,
//                        (cellManager.y[cellManager.links1[i]] - yOffset) * zoom,
//                        (cellManager.x[cellManager.links2[i]] - xOffset) * zoom,
//                        (cellManager.y[cellManager.links2[i]] - yOffset) * zoom
//                    )
//                }
//            }
//        }


        if (drawRays) {
            for (i in 0..<simulationSystem.specialCellsId) {
                val cellId = simulationSystem.drawSpecialCells[i]
                when (simulationSystem.cellType[cellId]) {
                    6 -> {
                        renderer.color = Color.CYAN
                        renderer.circle(
                            (simulationSystem.x[cellId] - xOffset) * zoom,
                            (simulationSystem.y[cellId] - yOffset) * zoom,
                            150f * zoom
                        )
                    }

                    14 -> {
                        renderer.color = getColorFromBits(simulationSystem.colorDifferentiation[cellId])
                        renderer.drawArrowWithRotationAngle(
                            startX = (simulationSystem.x[cellId] - xOffset) * zoom,
                            startY = (simulationSystem.y[cellId] - yOffset) * zoom,
                            baseAngle = simulationSystem.angle[cellId],
                            length = simulationSystem.visibilityRange[cellId] * zoom,
                            isDrawWithoutTriangle = true,
                        )
                    }

                    3, 9, 15, 21, 0 -> {
                        renderer.color = Color.CYAN
                        renderer.drawArrowWithRotationAngle(
                            startX = (simulationSystem.x[cellId] - xOffset) * zoom,
                            startY = (simulationSystem.y[cellId] - yOffset) * zoom,
                            baseAngle = simulationSystem.angle[cellId],// + cellManager.angleDiff[cellId],
                            length = 30f * zoom
                        )
                    }
                    else -> renderer.color = Color.CYAN
                }
            }
        }
        renderer.end()
    }

}
*/

fun getColorFromBits(bits: Int): Color {
    if (bits == 0) return Color.BLACK.cpy()

    var r = 0f
    var g = 0f
    var b = 0f
    var count = 0

    if (bits and 1 != 0) {
        r += 1f
        count++
    }
    if (bits and 2 != 0) {
        g += 1f
        count++
    }
    if (bits and 4 != 0) {
        b += 1f
        count++
    }

    return Color(r / count, g / count, b / count, 1f)
}

fun encodeColorToBits(r: Float, g: Float, b: Float): Int {
    var bits = 0
    if (r == 1f) bits = bits or 1
    if (g == 1f) bits = bits or 2
    if (b == 1f) bits = bits or 4
    return bits
}
