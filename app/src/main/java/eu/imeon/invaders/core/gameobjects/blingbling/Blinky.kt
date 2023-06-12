package eu.imeon.invaders.core.gameobjects.blingbling

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.SizeF
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.sin

class Blinky(val pos: PointF, private val radius: Float, private val period: Float, private val phaseShift: Float) {
    private var alphaNorm = 0f
    var t = 0f

    fun update(deltaT: Float) {
        t += deltaT

        alphaNorm = .5f + .5f * sin(phaseShift + t * period)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        val transformer = VScreen2CanvasTransformer(canvas)
        val screenPos = transformer.toCanvas(pos)
        val screenSize = transformer.toCanvas(SizeF(radius, radius))
        paint.color = Color.WHITE
        paint.alpha = (alphaNorm * 255).toInt()
        canvas.drawCircle(screenPos.x, screenPos.y, screenSize.width, paint)
    }
}