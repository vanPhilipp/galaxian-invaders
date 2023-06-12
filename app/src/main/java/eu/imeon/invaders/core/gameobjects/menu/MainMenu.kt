package eu.imeon.invaders.core.gameobjects.menu

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.playership.Controllable
import eu.imeon.invaders.core.gameobjects.status.AnchorV
import eu.imeon.invaders.core.gameobjects.status.TextUtil
import eu.imeon.invaders.core.gamestate.GameStatePersistence
import eu.imeon.invaders.core.gamestate.GlobalGameState

class MainMenu(
    theme: Theme,
    private val gameStatePersistence: GameStatePersistence,
    val gameEventLambda: GameEventLambda
) : GameObject, Controllable {

    private val paint = Paint()
    private val textUtil = TextUtil(theme.mainFont, theme.textColor, paint)
    private var currentIndex = 0
    private var difficultyLevel=0

    override fun draw(canvas: Canvas) {
        val variant = GameVariant.values()[currentIndex]
        textUtil.draw(canvas)
        textUtil.apply {
            center(1, "Pacman Vs. Asteroids")
            center(6, "Please Select")
            center(7, "Your Game")

            GameVariant.values().forEachIndexed { i, variant ->
                val row = 10 + i
                if (i == currentIndex) {
                    paint.alpha = 255
                    center(row, "<< ${variant.niceName} >>")
                } else {
                    paint.alpha = 128
                    center(row, "   ${variant.niceName}  ")
                }
            }

            paint.alpha = 255
            center(18, "Level: ${difficultyLevel + 1}")
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

    override fun onControllerFire() {
        GlobalGameState.gameVariant = GameVariant.values()[currentIndex]
        GlobalGameState.difficultyLevel=difficultyLevel
        gameEventLambda(GameEvent.StartNewGame, PointF(), 0f, null, 0)
    }

    override fun onControllerPan(dir: Int) {
        currentIndex = (currentIndex + dir).coerceIn(0 until GameVariant.values().size)
    }

    override fun onControllerTilt(dir: Int) {
        difficultyLevel= (difficultyLevel - dir).coerceIn(0 until 9)
    }
}
