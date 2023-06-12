package eu.imeon.invaders.core.gameobjects.particles


import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.SizeF
import androidx.core.graphics.minus
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.cos
import kotlin.math.sin

typealias PairF = Pair<Float, Float>

fun PairF.interpolate(t: Float): Float {
    return first + (second - first) * t.coerceIn(0f,1f)
}


class Photon(
    private val posStart: PointF,
    private val direction: PairF,
    private val rectExtend: PairF,
    private var speed: Float,
    private val lifetime: Float,
    private val color: Int,
    private val smoothing: Boolean = false,
    delaySec: Float
) : AbstractParticle() {

    private var t = -delaySec
    private val pos = posStart

    private fun normalizedTime(): PairF {
        val t1 = t.coerceAtLeast(0f)
        var tNorm = (t1 / lifetime).coerceIn(0f, 1f)
        if (smoothing)
            tNorm = sin(tNorm * Math.PI.toFloat() / 2f)
        return PairF(tNorm, 1f - tNorm)
    }

    override fun update(deltaT: Float): Boolean {
        val (tNorm, _) = normalizedTime()
        val t1=(tNorm).coerceIn(0f,1f)
        val dir = direction.interpolate(t1)
        pos.x = posStart.x + t1 * cos(dir) * speed
        pos.y = posStart.y + t1 * sin(dir) * speed

        t += deltaT
        return (t < lifetime)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val (tNorm, tFade) = normalizedTime()
        val transformer = VScreen2CanvasTransformer(canvas)
        val extend = rectExtend.interpolate(tNorm)
        val posCorrected = pos - PointF(extend / 2f, extend / 2f)
        val rect = transformer.toCanvas(posCorrected, SizeF(extend, extend))
        paint.color = color
        paint.alpha = (255f * tFade).toInt()
        canvas.drawRect(rect, paint)
    }
}
