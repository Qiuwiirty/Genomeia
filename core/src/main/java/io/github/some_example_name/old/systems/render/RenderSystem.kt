package io.github.some_example_name.old.systems.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import io.github.some_example_name.old.entities.CellEntity
import io.github.some_example_name.old.entities.LinkEntity
import io.github.some_example_name.old.entities.NeuralEntity
import io.github.some_example_name.old.entities.ParticleEntity
import io.github.some_example_name.old.entities.SimEntity
import kotlin.math.round

class RenderSystem(
    val tripleBufferManager: TripleBufferManager,
    val simEntity: SimEntity,
    val cellEntity: CellEntity,
    val linkEntity: LinkEntity,
    val particleEntity: ParticleEntity,
    val shaderManager: ShaderManager
) {

    val fontMatrix = Matrix4()

    fun create() {
        shaderManager.create()
    }

    fun drawShader(camera: Camera) {
        val (currentRead, isNewFrame) = tripleBufferManager.getAndSwapConsumer()
        shaderManager.render(
            currentRead = currentRead,
            cameraProjection = camera.combined,
            isNewFrame = isNewFrame
        )
    }
    fun drawTextSimInfo(spriteBatch: SpriteBatch, font: BitmapFont) {
        //TODO тут кстати тоже нужна синхронизация, хоть и не так критично

        val uiProjection = fontMatrix.setToOrtho2D(
            0f,
            0f,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )
        spriteBatch.projectionMatrix = uiProjection

        spriteBatch.begin()
        font.draw(
            spriteBatch,
            """
                    FPS: ${Gdx.graphics.framesPerSecond}
                    UPS: ${simEntity.ups}
                    Update Time: ${round(1e5f / simEntity.ups) / 100f} ms
                    Cells: ${cellEntity.lastId - cellEntity.deadStack.size + 1}
                    Particles: ${particleEntity.lastId - particleEntity.deadStack.size + 1}
                    Links ${linkEntity.lastId - linkEntity.deadStack.size + 1}
                    NeuronImpulseInput ${if (simEntity.grabbedCell != -1) cellEntity.getNeuronImpulseInput(simEntity.grabbedCell) else "0.0"}
                    NeuronImpulseOutput ${if (simEntity.grabbedCell != -1) cellEntity.getNeuronImpulseOutput(simEntity.grabbedCell) else "0.0"}
                """.trimIndent(),
            30f,
            140f
        )
        font.data.setScale(1f)
        spriteBatch.end()
    }

    fun dispose() {

    }
}
