package eu.imeon.invaders.core.gameobjects.maze

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.Size
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import kotlin.math.min

// A GameObject class that renders the PacMan maze


class PacManMaze : GameObject {

    var config=PacManMazeConfig()

    private val paintWall = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val paintGhostDoor = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val paintPill = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 1f
    }

    //private val asciiMaze="1234-|.O"

    private var maze = listOf(listOf<MazeNode>())
    var size: Size = Size(0, 0)
    var ghostHome: MazeNode = MazeNode(MazeItem.NONE)
    var pacmanHome: MazeNode = MazeNode(MazeItem.NONE)
    var nodeSize: Float = 0f
    private var org: PointF = PointF(0f, 0f)
    private var t = 0f
    private var pillCount = 0

    init {
        initMaze("~.P")
    }

    private fun initMaze(ascii: String) {
        maze = decodeAsciiMaze(ascii)
        size = Size(maze[0].size, maze.size)
        nodeSize = min(
            VScreen.width / size.width.toFloat(),
            VScreen.height / size.height.toFloat()
        )
        org = PointF(
            (VScreen.width - size.width * nodeSize) / 2f,
            (VScreen.height - size.width * nodeSize) / 2f
        )
        linkNodes()
    }

    fun getMazeNode(x: Int, y: Int): MazeNode? {
        if (y < 0 || y >= size.height)
            return null
        val row = maze[y]
        if (x < 0 || x >= row.size)
            return null
        return row[x]
    }

    fun logicalPos2VScreenPos(lp: PointF): PointF = PointF(
        org.x + lp.x * nodeSize,
        org.y + lp.y * nodeSize
    )


    private fun decodeAsciiMaze(ascii: String): List<List<MazeNode>> {
        return ascii.trimIndent().split("\n").map {
            it.map { c ->
                when (c) {
                    '1' -> MazeNode(MazeItem.ARC1)
                    '2' -> MazeNode(MazeItem.ARC2)
                    '3' -> MazeNode(MazeItem.ARC3)
                    '4' -> MazeNode(MazeItem.ARC4)
                    '|' -> MazeNode(MazeItem.VERTICAL_WALL)
                    '-' -> MazeNode(MazeItem.HORIZONTAL_WALL)
                    'O' -> MazeNode(MazeItem.POWER_PILL)
                    '.' -> MazeNode(MazeItem.PILL)
                    '~' -> MazeNode(MazeItem.GHOST_DOOR)
                    'P' -> MazeNode(MazeItem.PACMAN_HOME)
                    else -> MazeNode(MazeItem.NONE)
                }
            }
        }
    }

    private fun walkArray(processNode: (node: MazeNode, x: Int, y: Int) -> Unit) {
        for (y in 0 until size.height) {
            for (x in 0 until size.width) {
                val node = getMazeNode(x, y) ?: continue
                processNode(node, x, y)
            }
        }

    }

    private fun findNode(checkNode: (node: MazeNode) -> Boolean): MazeNode? {

        for (y in 0 until size.height) {
            for (x in 0 until size.width) {
                val node = getMazeNode(x, y) ?: continue
                if (checkNode(node))
                    return node
            }
        }
        return null
    }


    private fun linkNodes() {
        walkArray { node, x: Int, y: Int ->
            node.x = x
            node.y = y
        }
        var count = 0
        walkArray { node, _: Int, _: Int ->
            when (node.mazeItem) {
                MazeItem.PILL -> {
                    if (!config.smallPills) node.pillState = false
                }

                MazeItem.POWER_PILL -> {
                    if (!config.powerPills) node.pillState = false
                }

                else -> {}
            }

            if (node.pillState)
                count++
        }
        pillCount = count

        ghostHome = findNode { it.mazeItem == MazeItem.GHOST_DOOR }
            ?: throw InternalError("GHOST_DOOR not found in given maze")
        pacmanHome = findNode { it.mazeItem == MazeItem.PACMAN_HOME }
            ?: throw InternalError("PACMAN_HOME not found in given maze")
    }


    override fun prepareGame() {

    }

    override fun prepareLevel(difficultyLevel: Int) {
        initMaze(AsciiMazes.mazes[0])
    }

    override fun update(deltaT: Float) {
        t += deltaT
    }

    override fun registerHitables(superCollider: SuperCollider) {
        super.registerHitables(superCollider)
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        super.checkCollisions(superCollider)
    }

    private fun drawNodeArc(
        canvas: Canvas,
        rect0: RectF,
        angle0: Float,
        deltaX: Float,
        deltaY: Float
    ) {
        val rect1 = RectF(rect0)
        rect1.offset(deltaX, deltaY)
        canvas.drawArc(rect1, angle0, 90f, false, paintWall)
    }

    private fun drawNode(canvas: Canvas, node: MazeNode) {

        val tl = logicalPos2VScreenPos(
            PointF(
                node.x.toFloat(),
                node.y.toFloat()
            )
        )

        val transformer = VScreen2CanvasTransformer(canvas)
        val rect = transformer.toCanvas(
            RectF(tl.x, tl.y, tl.x + nodeSize, tl.y + nodeSize)
        )
        val deltaX = rect.width() * .5f
        val deltaY = rect.height() * .5f

        val pillTick = ((t * 4f).toInt() % 2) != 0
//        paint.apply { color = Color.MAGENTA }
//        canvas.drawRect(rect, paint)
        when (node.mazeItem) {

            MazeItem.HORIZONTAL_WALL -> {
                canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), paintWall)
            }

            MazeItem.GHOST_DOOR -> {
                canvas.drawLine(
                    rect.left,
                    rect.centerY(),
                    rect.right,
                    rect.centerY(),
                    paintGhostDoor
                )
            }

            MazeItem.VERTICAL_WALL -> {
                canvas.drawLine(rect.centerX(), rect.top, rect.centerX(), rect.bottom, paintWall)
            }

            MazeItem.ARC1 -> drawNodeArc(canvas, rect, 180f, deltaX, deltaY)

            MazeItem.ARC2 -> drawNodeArc(canvas, rect, 270f, -deltaX, deltaY)

            MazeItem.ARC3 -> drawNodeArc(canvas, rect, 90f, deltaX, -deltaY)

            MazeItem.ARC4 -> drawNodeArc(canvas, rect, 0f, -deltaX, -deltaY)

            MazeItem.PILL -> {
                val pillSize = rect.width() * .15f
                if (node.pillState)
                    canvas.drawCircle(rect.centerX(), rect.centerY(), pillSize, paintPill)
            }

            MazeItem.POWER_PILL -> {
                val powerPillSize = rect.width() * .4f
                if (node.pillState && pillTick)
                    canvas.drawCircle(rect.centerX(), rect.centerY(), powerPillSize, paintPill)
            }


            else -> {
            }
        }
    }

    override fun draw(canvas: Canvas) {
        maze.onEach { row ->
            row.onEach { node ->
                drawNode(canvas, node)
            }
        }
    }

    override fun drawDebug(canvas: Canvas) {
        super.drawDebug(canvas)
    }

    override fun readyForRespawn(superCollider: SuperCollider): Boolean {
        return super.readyForRespawn(superCollider)
    }

}