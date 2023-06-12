package eu.imeon.invaders.core.gameobjects.missiles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.SuperCollider

class MissileManager(theme:Theme) : GameObject {

    private var missiles = mutableListOf<Missile>()
    private var paint = Paint().apply { color = Color.WHITE }

    private val sprites=theme.missiles

    fun firePlayer(
        startPos: PointF,
        speedZero: PointF,
        creator: Hitable,
        alpha0: Float
    ) {
        missiles.add(Missile(startPos, speedZero, creator, sprites[0], alpha0))
    }

    fun fireInvader(
        startPos: PointF,
        speedZero: PointF,
        creator: Hitable
    ) {
        missiles.add(Missile(startPos, speedZero, creator, sprites[1],0f))
    }

    override fun update(deltaT: Float) {
        missiles.forEach { it.update(deltaT) }

        missiles = missiles.filter { it.active }.toMutableList()

    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.WHITE

        missiles.onEach { it.draw(canvas, paint) }
    }

    override fun drawDebug(canvas: Canvas) {
        paint.color = Color.WHITE

        missiles.onEach { it.drawDebug(canvas, paint) }
    }


    fun flush() {
        missiles.clear()
    }

    override fun prepareLevel(difficultyLevel: Int) {
        flush()
    }

    override fun prepareGame() {
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        missiles.forEach {
            it.clashDetector(superCollider)
        }
    }

    override fun readyForRespawn(superCollider: SuperCollider): Boolean {
        return missiles.size==0
    }
}
