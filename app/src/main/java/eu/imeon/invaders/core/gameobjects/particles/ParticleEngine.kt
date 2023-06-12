package eu.imeon.invaders.core.gameobjects.particles


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import eu.imeon.invaders.core.GameObject

class ParticleEngine : GameObject {
    private var particles = mutableListOf<AbstractParticle>()
    private var paint = Paint().apply { color = Color.WHITE }

    fun add(p: AbstractParticle) {
        particles.add(p)
    }

    override fun update(deltaT: Float) {
        particles = particles.filter { it.update(deltaT) }.toMutableList()
    }

    override fun draw(canvas: Canvas) {
        particles.forEach {
            it.draw(canvas, paint)
        }
    }

    override fun prepareLevel(difficultyLevel: Int) {
    }

    override fun prepareGame() {
        particles.clear()
    }
}
