package eu.imeon.invaders.core.gameobjects.shelters

import android.graphics.*
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import kotlin.random.Random

class Shelter(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    private val brickColumns: Int,
    private val brickRows: Int,
    private val gameEventLambda: GameEventLambda,
    cutTheCorners: Boolean = true

) {
    private val bricks = ArrayList<DefenceBrick>()

    private var brickLambda: brickLambda = { logicPos: Point, centerPos: PointF ->
        gameEventLambda(GameEvent.ShelterDamaged, centerPos, 1f, null,0)
        hitNeighbours(logicPos)
    }

    init {
        val brickWidth = width / brickColumns
        val brickHeight = height / brickRows

        for (k in 0 until brickRows) {
            val y0 = top + brickHeight * k

            for (i in 0 until brickColumns) {
                val x0 = left + brickWidth * i

                var isVisible = true
                if (cutTheCorners) {
                    val corner = brickColumns / 4
                    if (i + k < corner)
                        isVisible = false

                    val i1 = brickColumns - 1 - i
                    if (i1 + k < corner)
                        isVisible = false

                    if (k > (brickColumns * 2) / 3) {
                        if (i > brickColumns / 4 && i < (brickColumns * 3) / 4)
                            isVisible = false
                    }
                }

                bricks.add(
                    DefenceBrick(
                        Point(i, k),
                        x0,
                        y0,
                        brickWidth,
                        brickHeight,
                        isVisible,
                        brickLambda
                    )
                )
            }
        }
    }

    fun flush() {
        bricks.clear()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        bricks.onEach { it.draw(canvas, paint) }
    }

    private fun getBrickAt(x: Int, y: Int): DefenceBrick {
        return bricks[y * brickRows + x]
    }

    private fun hitNeighbours(center: Point) {
        val rangeX = 2
        val rangeY = 3
        for (x in center.x - rangeX..center.x + rangeX) {
            for (y in center.y - rangeY..center.y + rangeY) {
                if (x in 0 until brickColumns &&
                    y in 0 until brickRows
                ) {
                    if (Random.nextFloat() > .4f) {
                        getBrickAt(x, y).onHitSecondary()
                    }
                }

            }
        }
    }

    fun registerHitables(superCollider: SuperCollider) {
        bricks.forEach {
            if (it.isVisible())
                superCollider.register(it)
        }
    }
}

