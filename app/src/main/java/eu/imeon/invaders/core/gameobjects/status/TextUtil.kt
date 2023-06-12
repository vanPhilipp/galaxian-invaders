package eu.imeon.invaders.core.gameobjects.status

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.sqrt

class TextUtil(private val font: Typeface, private val textColor:Int, val paint:Paint) {

    private var rowHeight = 6f
    private var textHeight = 1f

    init {
        paint.textSize = 24f
        paint.typeface = font

    }

    private val marginX = 3f
    private val marginY = 1f
    private var canvas:Canvas= Canvas()

    private fun getAnchorPoint(
        canvas: Canvas,
        anchorH: AnchorH,
        anchorV: AnchorV,
        rowId: Int
    ): PointF {
        val pos = VScreen2CanvasTransformer(canvas).toCanvas(
            PointF(
                when (anchorH) {
                    AnchorH.Left -> marginX
                    AnchorH.Center -> VScreen.width / 2f
                    AnchorH.Right -> VScreen.width - marginX
                },
                when (anchorV) {
                    AnchorV.Top -> marginY + rowId * rowHeight
                    AnchorV.Middle -> VScreen.height / 2f + rowId * rowHeight
                    AnchorV.Bottom -> VScreen.height - marginY - rowId * rowHeight
                }
            )
        )
        pos.y += when (anchorV) {
            AnchorV.Top -> textHeight
            AnchorV.Middle -> textHeight / 2f
            AnchorV.Bottom -> 0f
        }
        return pos
    }

     fun left(rowId: Int, text: String, anchorV: AnchorV = AnchorV.Top) {
        paint.textAlign = Paint.Align.LEFT
        val pos = getAnchorPoint(canvas, AnchorH.Left, anchorV, rowId)
        canvas.drawText(text, pos.x, pos.y, paint)
    }

     fun center(rowId: Int, text: String, anchorV: AnchorV = AnchorV.Top) {
        paint.textAlign = Paint.Align.CENTER
        val pos = getAnchorPoint(canvas, AnchorH.Center, anchorV, rowId)
        canvas.drawText(text, pos.x, pos.y, paint)
    }

     fun right(rowId: Int, text: String, anchorV: AnchorV = AnchorV.Top) {
        paint.textAlign = Paint.Align.RIGHT
        val pos = getAnchorPoint(canvas, AnchorH.Right, anchorV, rowId)
        canvas.drawText(text, pos.x, pos.y, paint)
    }

    fun draw(canvas: Canvas){
        this.canvas=canvas
        paint.apply {
            color = textColor
            style = Paint.Style.FILL
            val diag = sqrt((canvas.height * canvas.height + canvas.width * canvas.width).toFloat())
            textSize = 12f / 930f * diag
        }
        val bounds = Rect()
        paint.getTextBounds("y1", 0, 2, bounds)

        textHeight = bounds.height().toFloat()

    }

    fun drawDebug(canvas: Canvas){
        val tl = getAnchorPoint(canvas, AnchorH.Left, AnchorV.Top, 0)
        val br = getAnchorPoint(canvas, AnchorH.Right, AnchorV.Bottom, 0)
        val rect = RectF(tl.x, tl.y, br.x, br.y)
        paint.color = Color.MAGENTA
        paint.style = Paint.Style.STROKE
        canvas.drawRect(rect, paint)

    }
}