package eu.imeon.invaders.core

import android.graphics.Canvas
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.gameobjects.playership.CheatCode

interface GameObject {
    // Begin of each game
    fun prepareGame()

    // Begin of new round (or wave)
    fun prepareLevel(difficultyLevel: Int)

    // Update internal state
    fun update(deltaT: Float)

    // Register all possible candidates for collisions in a quad-tree-map
    fun registerHitables(superCollider: SuperCollider) {}

    // Check possible collisions by calling superCollider.text
    fun checkCollisions(superCollider: SuperCollider) {}

    // Draw state
    fun draw(canvas: Canvas) {}

    // Drag debug infos
    fun drawDebug(canvas: Canvas) {}

    fun readyForRespawn(superCollider: SuperCollider):Boolean { return true }

    fun onCheatCode(cheatCode: CheatCode) {}
}