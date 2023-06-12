package eu.imeon.invaders.core.tests

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.KeyEvent
import eu.imeon.invaders.core.*
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState.Companion.waves
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.games.BaseGame
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.util.Sprite

class TextRenderTestRunner(theme:Theme, soundPlayer: SoundPlayer, context: Context) :
    BaseGame(
        theme,
        soundPlayer,
        context,
    ) {


    private val onGameEvent: GameEventLambda =
        { gameEvent: GameEvent, pos: PointF, severity: Float, sprite: Sprite?, textureId:Int ->

//            if (gameEvent.gameState != GameState.None)
//                nextGameState = gameEvent.gameState
            fxEngine.createFx(gameEvent, pos, severity, sprite, textureId)
        }

    override fun levelBuilder() {
        gameObject = arrayListOf<GameObject>(
            blingBlingStars,
            missileManager,
            particleEngine,
            superCollider,
        )
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        Log.i("debug","onKeyDown: ${KeyEvent.keyCodeToString(keyCode)}")
//        return when (keyCode) {
//            KeyEvent.KEYCODE_A -> {
//                val paint = Paint().apply {
//                    color = Color.WHITE
//                    typeface = mainFont
//                    textSize = 24F
//                }
//
//                fxEngine.renderBigCenterText("Magic", paint)
//                true
//            }
//            else -> false
//        }
//    }

}
