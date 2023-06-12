package eu.imeon.invaders.core.gameobjects.shelters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.vscreen.VScreen

class ShelterManager(private val gameEventLambda: GameEventLambda) : GameObject {

    private var paint = Paint().apply { color = Color.WHITE }
    private val shelters = ArrayList<Shelter>()

    override fun prepareLevel(difficultyLevel: Int) {

        val count = (5 - difficultyLevel).coerceIn(0, 4)
        flush()

        // Shelter location on virtual Screen
        val top = VScreen.vScaled(70f)
        val height = VScreen.vScaled(10f)

        // Number of Shelters
        val width = VScreen.hScaled(12f)
        val remains = VScreen.width - width * count
        val space = remains / (count + 1)

        for (id in 0 until count) {
            val x = space + (space + width) * id
            shelters.add(Shelter(x, top, width, height, 10, 10, gameEventLambda))
        }

        shelters.add(
            Shelter(
                0f,
                VScreen.invaderTouchDownLevel,
                VScreen.width,
                VScreen.vScaled(.5f),
                100,
                1,
                gameEventLambda,
                false
            )
        )


    }

    private fun flush() {
        shelters.onEach { it.flush() }
        shelters.clear()
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.GREEN
        shelters.onEach { it.draw(canvas, paint) }
    }


    override fun update(deltaT: Float) {
    }

    override fun prepareGame() {
    }

    override fun registerHitables(superCollider: SuperCollider) {
        shelters.forEach {
            it.registerHitables(superCollider)
        }
    }
}
