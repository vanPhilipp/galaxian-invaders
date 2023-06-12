package eu.imeon.invaders.core.gameobjects.status

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.gamestate.GameStatePersistence
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.sqrt




class StatusPresenter(
    theme: Theme,
    private val gameStatePersistence: GameStatePersistence
) : GameObject {

    private val paint=Paint()
    private val textUtil=TextUtil(theme.mainFont, theme.textColor, paint)

    override fun draw(canvas: Canvas) {

        textUtil.draw(canvas)
        textUtil.apply {
            left( 0, "SCORE")
            left( 1, GlobalGameState.score.toString())

            center( 0, "HI-SCORE")
            center( 1, gameStatePersistence.highScore.value.toString())

            left( 0, GlobalGameState.lives.toString(), AnchorV.Bottom)

            right( 0, "ROUND")
            val footerRight = String.format("%02d", GlobalGameState.waves + 1)
            right( 1, footerRight)

            right( 0, GlobalGameState.currentState.toString(), AnchorV.Bottom)
        }
    }

    override fun update(deltaT: Float) {
    }

    override fun prepareLevel(difficultyLevel: Int) {
    }

    override fun prepareGame() {
    }

    override fun drawDebug(canvas: Canvas) {
        textUtil.drawDebug(canvas)
    }
}
