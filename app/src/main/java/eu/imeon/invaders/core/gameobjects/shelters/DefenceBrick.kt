package eu.imeon.invaders.core.gameobjects.shelters

import android.graphics.*
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.gameobjects.invaders.Invader
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.random.Random

typealias brickLambda = (Point, PointF) -> Unit

fun RectF.quadrant(i: Int): RectF {
    val w2 = width() / 2
    val h2 = height() / 2
    val x = (i % 2)
    val y = ((i / 2) % 2)

    return RectF(
        left + w2 * x,
        top + h2 * y,
        left + w2 * (x + 1),
        top + h2 * (y + 1)
    )
}


class DefenceBrick(
    private val logicPos: Point,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    defaultVisibility: Boolean,
    val brickLambda: brickLambda,

    ) : Hitable {

    private val rect = RectF(x, y, x + width, y + height)
    private val center = PointF(x + width / 2, y + height / 2)
    private var strength = if (defaultVisibility) 3 else 0

    fun isVisible(): Boolean = (strength > 0)


    fun draw(canvas: Canvas, paint: Paint) {
        if (!isVisible())
            return

        val transformer = VScreen2CanvasTransformer(canvas)
        paint.color = Color.BLUE
        paint.style = Paint.Style.FILL

        when (strength) {
            1 -> {
                canvas.drawRect(transformer.toCanvas(rect.quadrant(1)), paint)
            }
            2 -> {
                canvas.drawRect(transformer.toCanvas(rect.quadrant(1)), paint)
                canvas.drawRect(transformer.toCanvas(rect.quadrant(2)), paint)
            }
            3 -> {
                canvas.drawRect(transformer.toCanvas(rect), paint)
            }
        }
    }

    private fun randomHarm() {
        strength = (strength - Random.nextInt(4) - 1).coerceAtLeast(0)
    }

    fun onHitSecondary() {
        randomHarm()
    }

    override fun getHitRect(): RectF {
        return rect
    }

    override fun onHit(source: Hitable): Boolean {
        if (!isVisible())
            return false

        if (source is Invader)
            strength = 0

        brickLambda(logicPos, center)
        randomHarm()
        return true
    }

    override fun performerType(): PerformerType {
        return PerformerType.Static
    }
}
