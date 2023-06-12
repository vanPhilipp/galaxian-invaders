package eu.imeon.invaders.core.gameobjects.ghosts

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.maze.Directions
import eu.imeon.invaders.core.gameobjects.maze.MazeItem
import eu.imeon.invaders.core.gameobjects.maze.MazeNode
import eu.imeon.invaders.core.gameobjects.maze.PacManMaze
import kotlin.math.sign
import kotlin.random.Random


private enum class GhostLiveCycle(val speed: Float) {
    AtHome(5f),
    Cruising(5f),
    GoingHome(10f),
}

class Ghost(
    val sprite: Sprite,
    private val gameEventLambda: GameEventLambda,
    private val maze: PacManMaze,
    private val pacman: Hitable
) : Hitable {

    private val size = sprite.size
    private val logicalPos = PointF(0f, 0f)
    private var t = 0f
    private var homeNode: MazeNode = maze.ghostHome
    private var currentNode: MazeNode = maze.ghostHome
    private var speed = 5f // Nodes per second
    private var direction: Directions = Directions.RIGHT

    // This will hold the pixels per second speed that the invader will move

    private var lifecycle = GhostLiveCycle.AtHome

    private fun canWeGoThere(node: MazeNode, dir: Directions): Boolean {
        val targetNode = maze.getMazeNode(node.x + dir.dx, node.y + dir.dy) ?: return false

        if (targetNode.mazeItem.isWall)
            return false

        if (targetNode.mazeItem == MazeItem.GHOST_DOOR) {
            if (lifecycle == GhostLiveCycle.Cruising)
                return false
        }

        return true
    }

    private fun findNewDirection(): Directions {
        val baseOffset = if (Random.nextBoolean()) 3 else 1
        for (i in 0..1) {
            val nextIndex = (direction.index + baseOffset + i * 2) % 4
            val nextDir = Directions.values()[nextIndex]
            if (canWeGoThere(currentNode, nextDir))
                return nextDir
        }
        throw InternalError("Can't define valid new direction for ghost")
    }

    private fun findNewRandomDirection(): Directions? {
        var target = pacman.isWhere()
        var fleeing = false
        when (lifecycle) {
            GhostLiveCycle.AtHome -> {
                return if (canWeGoThere(currentNode, Directions.UP) && Random.nextFloat() > .9f)
                    Directions.UP
                else
                    null
            }

            GhostLiveCycle.Cruising -> {
                if (pacman.hasSuperpower()) fleeing = true
            }

            GhostLiveCycle.GoingHome -> {
                target = Point(homeNode.x, homeNode.y)
            }
        }

        val baseOffset = if (Random.nextBoolean()) 3 else 1
        for (i in 0..1) {
            val index0 = (direction.index + baseOffset + i * 2) % 4
            val dir0 = Directions.values()[index0]
            if (canWeGoThere(currentNode, dir0)) {

                val p0 =
                    (target.x - currentNode.x).sign * (dir0.dx).sign + (target.y - currentNode.y).sign * (dir0.dy).sign
                // returns 1 for good directions, -1 for bad direction and 0 for indifferent

                if (lifecycle == GhostLiveCycle.GoingHome && p0 == 1)
                    return dir0

                var p1 = .5f + .4f * p0
                // return high probability for good directions and low probability for bad directions.


                if (fleeing)
                    p1 = 1f - p1
                // Ensures that for probabilities are inverted when we must flee

                if (Random.nextFloat() < p1)
                    return dir0
            }
        }

        return null
    }

    fun update(deltaT: Float) {
        t += deltaT
        speed = lifecycle.speed
        if (lifecycle == GhostLiveCycle.Cruising && pacman.hasSuperpower())
            speed *= .5f

        val tNorm = (t * speed).coerceIn(0f, 1f)
        val x = currentNode.x + direction.dx * tNorm
        val y = currentNode.y + direction.dy * tNorm
        logicalPos.set(x, y)
        if (tNorm >= 1f) {
            t = 0f
            // Target node reached
            val mx0 = currentNode.x + direction.dx
            val my0 = currentNode.y + direction.dy
            val nextNode = maze.getMazeNode(mx0, my0)
            if (nextNode != null) {
                when (lifecycle) {
                    GhostLiveCycle.AtHome -> {
                        if (currentNode.mazeItem == MazeItem.GHOST_DOOR && nextNode.mazeItem != MazeItem.GHOST_DOOR)
                            lifecycle = GhostLiveCycle.Cruising
                    }

                    GhostLiveCycle.Cruising -> {}
                    GhostLiveCycle.GoingHome -> {
                        if (currentNode.mazeItem == MazeItem.GHOST_DOOR && nextNode.mazeItem != MazeItem.GHOST_DOOR)
                            lifecycle = GhostLiveCycle.AtHome
                    }
                }


                currentNode = nextNode
                val maxX = maze.size.width - 1
                val cx = currentNode.x
                val cy = currentNode.y
                when {
                    (cx == 0 && direction == Directions.LEFT) -> {
                        currentNode = maze.getMazeNode(maxX, cy)
                            ?: throw InternalError("Tunnel crossing failed (left)")
                    }

                    (cx == maxX && direction == Directions.RIGHT) -> {
                        currentNode = maze.getMazeNode(0, cy)
                            ?: throw InternalError("Tunnel crossing failed (right)")
                    }
                }

            } else {
                throw InternalError("Ghost walked onto invalid null-node")
            }

            direction = if (!canWeGoThere(currentNode, direction)) {
                findNewDirection()
            } else {
                findNewRandomDirection() ?: direction
            }
        }
    }


    fun draw(canvas: Canvas, paint: Paint) {
        val pos = maze.logicalPos2VScreenPos(logicalPos)
        var alpha1 = 255
        var id = 0
        when (lifecycle) {
            GhostLiveCycle.AtHome -> {
                if (pacman.hasSuperpower())
                    id = 2
            }

            GhostLiveCycle.Cruising -> {
                if (pacman.hasSuperpower())
                    id = 2
            }

            GhostLiveCycle.GoingHome -> {
                alpha1 = 64
                id = 2
            }
        }
        paint.apply {
            alpha = alpha1
        }
        sprite.draw(canvas, paint, pos, id)
    }


    private fun boundingRect(): RectF {
        val pos = currentPos()
        return RectF(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
    }

    private fun currentPos(): PointF {
        return maze.logicalPos2VScreenPos(logicalPos)
    }

    override fun getHitRect(): RectF {
        return boundingRect()
    }

    override fun onHit(source: Hitable): Boolean {
        if (source.performerType() != PerformerType.Player)
            return false

        if (lifecycle != GhostLiveCycle.Cruising)
            return false

        if (!pacman.hasSuperpower())
            return false

        destroy()
        return true
    }

    private fun destroy() {
        lifecycle = GhostLiveCycle.GoingHome
        //gameEventLambda(GameEvent.AlienExplodes, currentPos(), 0f, sprite, 2)
    }

    override fun performerType(): PerformerType {
        return PerformerType.Aggressor
    }
}
