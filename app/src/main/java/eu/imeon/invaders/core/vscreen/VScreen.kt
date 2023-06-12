package eu.imeon.invaders.core.vscreen

import android.graphics.PointF
import android.graphics.RectF
import android.util.SizeF

fun RectF.resize(factor: Float):RectF {
    val w0 = width()
    val h0 = height()
    val cx = centerX()
    val cy = centerY()
    val w1 = w0 * factor
    val h1 = h0 * factor
    left = cx - w1 / 2F
    right = cx + w1 / 2F
    top = cy - h1 / 2F
    bottom = cy + h1 / 2F
    return this
}

enum class VScreenOrientation {
    HORIZONTAL,
    VERTICAL
}

object VScreen {
    // The whole game play is calculated on a virtual screen with the given size.
    // Only during the View.draw(...) calls it's transformed to the real canvas size

    const val TOUCH_GRID_X=4
    const val TOUCH_GRID_Y=4
    const val joystickAreaCount=4

    private const val lengthA=90f
    private const val lengthB=160f
    var size = SizeF(lengthA, lengthB)
    var orientation=VScreenOrientation.VERTICAL
        set(value) {
            size = if (orientation==VScreenOrientation.VERTICAL)
                SizeF(lengthA, lengthB)
            else
                SizeF(lengthB, lengthA)
            field = value
        }

    val rect = RectF(0f, 0f, size.width, size.height)

    val width: Float
        get() = size.width

    val height: Float
        get() = size.height

    val centerX:Float
        get()=size.width/2f

    val centerY:Float
        get()=size.height/2f

    val invaderTouchDownLevel: Float
        get() = vScaled(99f)

    // Reserve a part of the real canvas for the touch-joystick
    const val touchBottomArea = .8f
    const val touchTopArea = .2f

    fun hScaled(percent: Float): Float {
        return size.width * percent / 100.0f
    }

    fun vScaled(percent: Float): Float {
        return size.height * percent / 100.0f
    }


    fun scaledPointF(xPercent: Float, yPercent: Float): PointF {
        return PointF(hScaled(xPercent), vScaled(yPercent))
    }

    fun scaledSizeF(xPercent: Float, yPercent: Float): SizeF {
        return SizeF(hScaled(xPercent), vScaled(yPercent))
    }

    fun isPointOnVirtualScreen(point: PointF): Boolean {
        return rect.contains(point.x, point.y)
    }


}