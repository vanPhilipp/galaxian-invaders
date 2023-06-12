package eu.imeon.invaders.core.gameobjects.asteroids

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

typealias OnHitCallback = (asteroid:Asteroid)->Unit

class Asteroid(
    startPos: PointF,
    val direction: Float,
    val speed: Float,
    private val omega: Float,
    val size: Float,
    private val onHitCallback: OnHitCallback,
    val generationId:Int,
    private val gameEventLambda: GameEventLambda,
    private val performerType: PerformerType
) : Hitable {

    val pos = PointF(startPos.x, startPos.y)
    private var t = 0f

    var active = true
        private set

    private val path = Path()
    private val edges = mutableListOf<PointF>()

    private var alpha = 0f

    init {
        val count = 16
        val pi = PI.toFloat()
        val alphaRange = .3f * (2f * pi / count.toFloat())
        for (i in 0 until count) {
            val beta0 = 2f * pi * i.toFloat() / count
            val beta1 = Random.nextFloat() * alphaRange
            val radius = size + Random.nextFloat() * size * .5f
            edges.add(PointF(beta0 + beta1, radius))
        }
    }

    private fun updatePos(pos:Float, delta:Float, screenSize:Float, size:Float):Float{
        val pos1=pos + delta
        return when {
            delta>0 -> if (pos1>screenSize+size) -size else pos1
            delta<0 -> if (pos1<-size) screenSize+size else pos1
            else -> pos1
        }
    }

    fun update(deltaT: Float) {
        t += deltaT
        alpha += omega * deltaT

        val dx=cos(direction)*speed*deltaT
        val dy=sin(direction)*speed*deltaT

        pos.set(
            updatePos(pos.x, dx, VScreen.width, size),
            updatePos(pos.y, dy, VScreen.height, size)
        )
    }


    fun draw(canvas: Canvas, paint: Paint) {

        val transformer = VScreen2CanvasTransformer(canvas)
        path.reset()
        for (i in 0..edges.size) {
            val edge = edges[i % edges.size]
            val (beta, radius) = edge
            val gamma = alpha + beta
            val p = transformer.toCanvas(
                PointF(
                    pos.x + cos(gamma) * radius,
                    pos.y + sin(gamma) * radius
                )
            )
            if (i == 0)
                path.moveTo(p.x, p.y)
            else
                path.lineTo(p.x, p.y)
        }
        paint.apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            alpha=64
        }
        canvas.drawPath(path, paint)
        paint.apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 3f
            alpha=255
        }
        canvas.drawPath(path, paint)
    }

    private fun boundingRect(): RectF {
        return RectF(pos.x - size, pos.y - size, pos.x + size, pos.y + size)
    }

    override fun getHitRect(): RectF {
        return boundingRect()
    }

    override fun onHit(source: Hitable): Boolean {
        if (source.performerType() != PerformerType.Player)
            return false

        onHitCallback(this)
        destroy()
        return true
    }

    private fun destroy() {
        active = false
        val pos=PointF(pos.x, pos.y)
        gameEventLambda(GameEvent.AsteroidExplodes,pos,(generationId+1f), null,0)
    }

    override fun performerType(): PerformerType {
        return performerType
    }
}
