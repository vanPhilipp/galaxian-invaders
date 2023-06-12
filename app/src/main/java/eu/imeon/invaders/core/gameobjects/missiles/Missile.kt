package eu.imeon.invaders.core.gameobjects.missiles

import android.graphics.*
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer

class Missile(val pos: PointF, speedZero: PointF, private val creator: Hitable, val sprite: Sprite, val alpha0:Float) {
    private var speed = PointF(speedZero.x, speedZero.y)
    var active = true

    fun update(deltaT: Float) {
        if (!active)
            return

        pos.x += speed.x * deltaT
        pos.y += speed.y * deltaT
        if (!VScreen.isPointOnVirtualScreen(pos) || pos.y > VScreen.invaderTouchDownLevel) {
            active = false
            return
        }
    }

    private fun boundingRect(): RectF {
        val size=sprite.size
        return RectF(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (!active)
            return

        sprite.drawRotated(canvas, paint, pos, alpha0)
    }

    fun drawDebug(canvas: Canvas, paint: Paint) {
        if (!active)
            return

        paint.apply {
            color=Color.YELLOW
            style=Paint.Style.STROKE
        }
        val rect=VScreen2CanvasTransformer(canvas).toCanvas(boundingRect())
        canvas.drawRect(rect, paint)
    }

    fun clashDetector(superCollider: SuperCollider) {
        superCollider.test(boundingRect()) { target: Hitable ->
            if (target.onHit(creator))
                active = false
        }
    }
}
