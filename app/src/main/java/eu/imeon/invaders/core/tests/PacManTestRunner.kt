package eu.imeon.invaders.core.tests

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.KeyEvent
import eu.imeon.invaders.core.*
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.asteroids.AsteroidSwarm
import eu.imeon.invaders.core.gameobjects.galactoids.GalactoidSwarm
import eu.imeon.invaders.core.gameobjects.ghosts.GhostSwarm
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState.Companion.waves
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.gameobjects.maze.PacManMaze
import eu.imeon.invaders.core.gameobjects.playership.Pacman
import eu.imeon.invaders.core.games.BaseGame
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.util.Engine
import eu.imeon.invaders.core.util.Sprite
import kotlinx.coroutines.DelicateCoroutinesApi

class PacManTestRunner(theme: Theme, soundPlayer: SoundPlayer, context: Context) :
    BaseGame(
        theme,
        soundPlayer,
        context,
    ) {


    private val gameEventLambdaTest: GameEventLambda =
        { gameEvent: GameEvent, pos: PointF, severity: Float, sprite: Sprite?, textureId: Int ->

//            if (gameEvent.gameState != GameState.None)
//                nextGameState = gameEvent.gameState
            fxEngine.createFx(gameEvent, pos, severity, sprite, textureId)
        }


    private val pacManMaze= PacManMaze()
    private val pacman= Pacman(theme, gameEventLambda, pacManMaze)
    private val ghostSwarm=GhostSwarm(theme, gameEventLambda, pacManMaze, pacman)

    override fun levelBuilder() {
        gameObject = arrayListOf<GameObject>(
            blingBlingStars,
            missileManager,
            particleEngine,
            superCollider,
            pacManMaze,
            ghostSwarm,
            pacman
        )
    }



}
