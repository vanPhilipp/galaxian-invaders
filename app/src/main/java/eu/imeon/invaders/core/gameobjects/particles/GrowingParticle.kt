package eu.imeon.invaders.core.gameobjects.particles


import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.SizeF
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.cos
import kotlin.math.sin


class GrowingParticle(
    x: Float,
    y: Float,
    private val particleSize0: Float,
    private val particleSize1: Float,
    alphaDeg0: Float,
    alphaDeg1: Float,
    private var radiusMax: Float,
    private val lifetime: Float,
    val color: Int,
    private val dropRange: Float
) : AbstractParticle() {

    private var t = 0f
    private val pos = PointF(x, y)
    private val posStart = PointF(x, y)
    private val alpha0 = Math.toRadians(alphaDeg0.toDouble()).toFloat()
    private val alpha1 = Math.toRadians(alphaDeg1.toDouble()).toFloat()

    override fun update(deltaT: Float): Boolean {
        val tNorm = (t / lifetime).coerceIn(0f, 1f)
        val tSin = sin(2f * tNorm / Math.PI.toFloat())
        val alphaNow = alpha0 + (alpha1 - alpha0) * tNorm
        pos.x = posStart.x + tSin * cos(alphaNow) * radiusMax
        pos.y = posStart.y + tSin * sin(alphaNow) * radiusMax + tNorm*tNorm*dropRange
        t += deltaT
        return (t < lifetime)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val transformer = VScreen2CanvasTransformer(canvas)
        val tNorm = (t / lifetime).coerceIn(0f, 1f)
        val tFade = 1f - tNorm
        val radius = particleSize0 + (particleSize1 - particleSize0) * tNorm
        val rect = transformer.toCanvas(pos, SizeF(radius, radius))

        paint.color = color
        paint.alpha = (255f * tFade).toInt()
//        canvas.drawCircle(pos.x, pos.y, rScreen, paint)
        canvas.drawRect(rect, paint)
    }

}
