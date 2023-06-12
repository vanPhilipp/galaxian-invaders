package eu.imeon.invaders.core.vscreen

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.SizeF

class VScreen2CanvasTransformer(val canvas: Canvas) {

    private val borders = RectF()

    init {
        val vRatio = VScreen.width / VScreen.height
        val cRatio = canvas.width.toFloat() / canvas.height.toFloat()
        if (cRatio > vRatio) { // Target canvas is wider than virtual screen
            val rWidth = canvas.height.toFloat() * vRatio
            val rHeight = canvas.height.toFloat()
            val left = (canvas.width.toFloat() - rWidth) / 2f
            val top = 0f
            borders.set(left, top, left + rWidth, top + rHeight)
        } else {
            val rHeight = canvas.width.toFloat() / vRatio
            val rWidth = canvas.width.toFloat()
            val top = (canvas.height.toFloat() - rHeight) / 2f
            val left = 0f
            borders.set(left, top, left + rWidth, top + rHeight)
        }
        borders.bottom = borders.top + borders.height() * VScreen.touchBottomArea
    }

    private fun hScale(x: Float): Float {
        return x / VScreen.width * borders.width()
    }

    private fun vScale(y: Float): Float {
        return y / VScreen.height * borders.height()
    }


    fun toCanvas(r: RectF): RectF {
        val left = borders.left + hScale(r.left)
        val top = borders.top + vScale(r.top)
        val right = borders.left + hScale(r.right)
        val bottom = borders.top + vScale(r.bottom)
        return RectF(left, top, right, bottom)
    }

    fun toCanvas(pos: PointF, size: SizeF): RectF {
        val left = borders.left + hScale(pos.x)
        val top = borders.top + vScale(pos.y)
        val width = hScale(size.width)
        val height = vScale(size.height)
        return RectF(left, top, left + width, top + height)
    }

    fun toCanvas(r: PointF): PointF {
        val x = borders.left + hScale(r.x)
        val y = borders.top + vScale(r.y)
        return PointF(x, y)
    }

    fun toCanvas(r: SizeF): SizeF {
        // Don't include the borders here!
        val x = hScale(r.width)
        val y = vScale(r.height)
        return SizeF(x, y)
    }

    fun canvasJoystickArea(id: Int): RectF {
        val width = borders.width() / VScreen.joystickAreaCount
        val y0 = borders.top + borders.height() * VScreen.touchBottomArea
        val x0 = id * width
        return RectF(x0, y0, x0 + width, borders.bottom)
    }

}