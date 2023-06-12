package eu.imeon.invaders.core.gameobjects.ghosts

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.maze.PacManMaze

class GhostSwarm(
    val theme: Theme,
    val gameEventLambda: GameEventLambda,
    private val pacManMaze: PacManMaze,
    val pacmanLocation: Hitable
) : GameObject {

    private var ghosts = mutableListOf<Ghost>()
    private val sprites = theme.invaders
    private var paint = Paint().apply { color = Color.WHITE }

    override fun prepareLevel(difficultyLevel: Int) {
        ghosts.clear()
        val homeNode = pacManMaze.ghostHome
        for (k in 0 until 4) {
            ghosts.add(
                Ghost(
                    sprites[k % theme.invaders.size],
                    gameEventLambda,
                    pacManMaze,
                    pacmanLocation
                )
            )
        }
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(255, 0, 255, 0)

        ghosts.forEach { it.draw(canvas, paint) }
    }


    override fun update(deltaT: Float) {

//        if (GlobalGameState.currentState != GameState.WaveInProgress)
//            return

        ghosts
            .forEach { it.update(deltaT) }
    }


    override fun prepareGame() {
    }

    override fun registerHitables(superCollider: SuperCollider) {
        ghosts.forEach {
            superCollider.register(it)
        }
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        ghosts.forEach {
            superCollider.test(it.getHitRect()) { target: Hitable ->
                target.onHit(it)
            }
        }
    }

    override fun readyForRespawn(superCollider: SuperCollider): Boolean {
        return true
    }


}