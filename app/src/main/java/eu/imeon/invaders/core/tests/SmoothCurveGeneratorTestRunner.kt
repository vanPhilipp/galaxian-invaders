package eu.imeon.invaders.core.tests

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import eu.imeon.invaders.core.*
import eu.imeon.invaders.core.games.BaseGame
import eu.imeon.invaders.core.gamestate.GlobalGameState.Companion.waves
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.util.SmoothCurveGenerator
import kotlin.math.min

class SmoothCurveGeneratorTestRunner(theme: Theme, soundPlayer: SoundPlayer, context: Context) :
    BaseGame(
        theme,
        soundPlayer,
        context,
    ) {

    private val path = Path()
    private val smoothCurveGenerator = SmoothCurveGenerator(.1, .1, 1.0)

    init {
        gameObject = arrayListOf<GameObject>(
            blingBlingStars,
            missileManager,
            particleEngine,
            superCollider,
        )
    }


    override fun levelBuilder() {
        gameObject.forEach { it.prepareLevel(waves,) }
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rectSize = (min(canvas.width, canvas.height)*.9f)

        val margins = PointF(
            (canvas.width - rectSize) / 2.0f,
            (canvas.height - rectSize) / 2.0f
        )

        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        val rect=RectF(margins.x, margins.y, margins.x+rectSize, margins.y+rectSize)
        canvas.drawRect(rect, paint)

        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawText("Test", margins.x, margins.y, paint)

        val count = 100
        path.reset()
        for (i in 0 until count) {
            val t = i.toFloat() / (count - 1).toFloat()
            val h = smoothCurveGenerator.calc(t)
            val x = margins.x + rectSize * t
            val y = margins.y + rectSize * (1f - h)
            if (i==0)
                path.moveTo(x,y)
            else
                path.lineTo(x, y)
        }

        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
    }
}
