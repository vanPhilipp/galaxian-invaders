package eu.imeon.invaders.core.gameobjects.asteroids

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.vscreen.VScreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AsteroidSwarm(
    val theme: Theme,
    val gameEventLambda: GameEventLambda,
) : GameObject {

    var config= AsteroidSwarmConfig()

    private var asteroids = mutableListOf<Asteroid>()
    private var paint = Paint().apply { color = Color.WHITE }
    private var startCount = 0


    private fun createChild(asteroid: Asteroid, generationId: Int) {
        val pi2 = 2f * PI.toFloat()
        val piH = .5f * PI.toFloat()
        val x = asteroid.pos.x
        val y = asteroid.pos.y
        val dir = asteroid.direction - 1f + 2f*Random.nextFloat()
        val speed = asteroid.speed * (1f+Random.nextFloat()*.6f)
        val nx = cos(asteroid.direction + piH) * asteroid.speed * .3f
        val ny = sin(asteroid.direction + piH) * asteroid.speed * .3f
        val pos = PointF(x + nx, y + ny)
        val omega = pi2 * (.5f + Random.nextFloat() * .5f)
        val size = asteroid.size * .7f
        asteroids.add(
            Asteroid(
                pos,
                dir,
                speed,
                omega,
                size,
                onHitCallback,
                generationId,
                gameEventLambda,
                config.performerType
            )
        )
    }

    private val onHitCallback: OnHitCallback = fun(asteroid: Asteroid): Unit {
        val generationId = asteroid.generationId + 1
        if (generationId > 3)
            return

        for (i in 0 until 2) {
            createChild(asteroid, generationId)
        }
    }


    override fun prepareLevel(difficultyLevel: Int) {
        asteroids.clear()

        val count = 3 + difficultyLevel/4
        for (i in 0 until count) {
            val size = VScreen.width * .05f
            val pos = PointF(
                if (Random.nextBoolean()) -size else VScreen.width + size,
                Random.nextFloat() * VScreen.height
            )
            val pi = PI.toFloat()
            val pi2 = 2 * pi
            val dir = Random.nextFloat() * pi2

            val speed = 4f + Random.nextFloat() * 3f
            val omega = -pi + pi2 * (.2f + Random.nextFloat() * .5f)
            asteroids.add(Asteroid(pos, dir, speed, omega, size, onHitCallback, 0, gameEventLambda,config.performerType))

        }
        startCount = asteroids.size
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(255, 0, 255, 0)

        asteroids.forEach { it.draw(canvas, paint) }
    }

    override fun update(deltaT: Float) {

        asteroids
            .forEach { it.update(deltaT) }

        val sizeOld = asteroids.size

        asteroids = asteroids
            .filter { it.active }
            .toMutableList()

        if (asteroids.size == 0 && sizeOld > 0) {
            gameEventLambda(GameEvent.WaveCompleted, PointF(), 1f, null, 0)
        }
    }

    override fun prepareGame() {
    }

    override fun registerHitables(superCollider: SuperCollider) {
        asteroids.forEach {
            if (it.active)
                superCollider.register(it)
        }
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        asteroids.forEach {
            superCollider.test(it.getHitRect()) { target: Hitable ->
                target.onHit(it)
            }
        }
    }

}
