package eu.imeon.invaders.core.gameobjects.particles


import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.SizeF
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.cos
import kotlin.math.sin

class SimpleParticle(
    x: Float,
    y: Float,
    private val radius: Float,
    alphaDeg: Float,
    private var speed: Float,
    private val lifetime: Float,
    val color: Int
) : AbstractParticle() {

    private var t = 0f
    private val pos = PointF(x, y)
    private val alpha = Math.toRadians(alphaDeg.toDouble()).toFloat()

    override fun update(deltaT: Float): Boolean {
        pos.x += (cos(alpha) * speed * deltaT).toFloat()
        pos.y += (sin(alpha) * speed * deltaT).toFloat()

        t += deltaT
        return (t < lifetime)
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val transformer = VScreen2CanvasTransformer(canvas)
        val rect = transformer.toCanvas(pos, SizeF(radius, radius))
        val tNorm = 1f - t / lifetime

        paint.color = color
        paint.alpha = (255f * tNorm).toInt()
//        canvas.drawCircle(pos.x, pos.y, rScreen, paint)
        canvas.drawRect(rect, paint)
    }

}
