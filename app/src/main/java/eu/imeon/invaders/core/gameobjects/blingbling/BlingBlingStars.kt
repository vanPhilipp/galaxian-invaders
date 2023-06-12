package eu.imeon.invaders.core.gameobjects.blingbling

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.vscreen.VScreen
import kotlin.random.Random

class BlingBlingStars(count: Int = 100) : GameObject {

    private val blinkies = mutableListOf<Blinky>()
    private val paint = Paint()

    init {
        for (i in 0 until count) {
            val point = PointF(
                Random.nextFloat() * VScreen.width,
                Random.nextFloat() * VScreen.height / 2,
            )
            blinkies.add(
                Blinky(
                    point,
                    .1f,
                    (.9f + .5f * Random.nextFloat()) * Math.PI.toFloat() * 2f,
                    Random.nextFloat() * Math.PI.toFloat() * 2f,
                )
            )
        }
    }

    override fun update(deltaT: Float) {
        blinkies.forEach { it.update(deltaT) }
    }

    override fun draw(canvas: Canvas) {
        blinkies.forEach { it.draw(canvas, paint) }
    }

    override fun prepareLevel(difficultyLevel: Int) {
    }

    override fun prepareGame() {
    }
}