package eu.imeon.invaders.core.gameobjects.invaders

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.graphics.plus
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.vscreen.VScreen

private enum class InvaderLiveCycle {
    Hidden,
    FadingIn,
    Active,
    Dead
}

class Invader(
    x: Float,
    y: Float,
    val sprite: Sprite,
    private val gameEventLambda: GameEventLambda,
    private val startupDelay: Float,
    private val fadeInLength: Float,
    private val severity: Float
) : Hitable {

    private val size = sprite.size
    private val relativePos = PointF(x - size.width / 2, y - size.height / 2)
    private val pos = PointF(0f, 0f)
    private var t = 0f

    init {
        updatePos()
    }

    // This will hold the pixels per second speed that the invader will move

    private var lifecycle = InvaderLiveCycle.Hidden

    val active
        get() = lifecycle == InvaderLiveCycle.Active || lifecycle == InvaderLiveCycle.FadingIn

    val alive
        get() = lifecycle != InvaderLiveCycle.Dead

    private fun switchLifeCycle(lc: InvaderLiveCycle) {
        lifecycle = lc
        t = 0f
    }

    fun update(deltaT: Float) {
        t += deltaT

        when (lifecycle) {
            InvaderLiveCycle.Hidden -> if (t >= startupDelay) switchLifeCycle(InvaderLiveCycle.FadingIn)
            InvaderLiveCycle.FadingIn -> if (t >= fadeInLength) switchLifeCycle(InvaderLiveCycle.Active)
            else -> {}
        }
        updatePos()
    }

    private fun updatePos() {
        pos.set(InvaderSwarm.swarmOrg + relativePos)
    }

    fun testBumpLeftRight(direction: Int, nextOrg: PointF): Int {
        // Check if an alien would move out of screen borders - and which border is touched
        if (!alive)
            return 0

        val x = nextOrg.x + relativePos.x
        if (direction < 0 && x < 0) {
            return 1
        }
        val maxPos = VScreen.width - size.width
        if (direction > 0 && x > maxPos)
            return -1

        return 0
    }

    fun testTouchDown(nextOrg: PointF): Boolean {
        // Check if an alien would move out of screen borders - and which border is touched
        if (!alive)
            return false

        val y = nextOrg.y + relativePos.y
        return (y + size.height >= VScreen.invaderTouchDownLevel)
    }

    fun getCurrentBitmapId():Int = if (InvaderSwarm.tickTock) 1 else 0

    fun draw(canvas: Canvas, paint: Paint) {

        paint.alpha = when (lifecycle) {
            InvaderLiveCycle.FadingIn -> (t / fadeInLength * 255f).toInt()
            InvaderLiveCycle.Active -> 255
            else -> return
        }
        sprite.draw(canvas, paint, pos, getCurrentBitmapId())
    }

    fun getFireOrg(): PointF {
        return PointF(pos.x + size.width / 2, pos.y + size.height)
    }

    private fun boundingRect(): RectF {
        return RectF(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
    }

    private fun currentPos(): PointF {
        return InvaderSwarm.swarmOrg + relativePos
    }

    override fun getHitRect(): RectF {
        return boundingRect()
    }

    override fun onHit(source: Hitable): Boolean {
        if (source.performerType() != PerformerType.Player)
            return false

        destroy()
        return true
    }

    fun destroy(){
        lifecycle = InvaderLiveCycle.Dead
        gameEventLambda(GameEvent.AlienExplodes, currentPos(), severity, sprite, getCurrentBitmapId())
    }

    override fun performerType(): PerformerType {
        return PerformerType.Aggressor
    }
}
