package eu.imeon.invaders.core.gameobjects.asteroids

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer

class AsteroidsRevivalExaminer:GameObject {

    private val rect = RectF(
        VScreen.width * .3f,
        VScreen.height * .4f,
        VScreen.width * .7f,
        VScreen.height * .6f,
    )
    private var t=0f
    private var respawnCollisionEvent=0f
    var paint=Paint()


    override fun draw(canvas: Canvas) {
        if (respawnCollisionEvent<=0f)
            return

        val decayTime=1.5f
        val delta=t-respawnCollisionEvent
        if (delta>=decayTime)
            return

        val tNorm=(1f - delta/decayTime).coerceIn(0f, 1f)
        val alpha=(tNorm*255f).toInt()
        val transformer = VScreen2CanvasTransformer(canvas)
        paint.apply{
            color = Color.argb(alpha, 255, 128, 128)
            style=Paint.Style.STROKE
        }
        val rectScreen=transformer.toCanvas(rect)
        canvas.drawRect(rectScreen, paint)
    }

    override fun readyForRespawn(superCollider: SuperCollider): Boolean {
        var ready = true
        superCollider.test(rect) { target: Hitable ->
            if (target.performerType() == PerformerType.Aggressor) {
                ready = false
                respawnCollisionEvent=t
            }
        }
        return ready
    }

    override fun prepareGame() {

    }

    override fun prepareLevel(difficultyLevel: Int) {
        t=0f
        respawnCollisionEvent=0f
    }

    override fun update(deltaT: Float) {
        t+=deltaT
    }
}