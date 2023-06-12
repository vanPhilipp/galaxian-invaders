package eu.imeon.invaders.core.gameobjects.playership

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.maze.Directions
import eu.imeon.invaders.core.gameobjects.maze.MazeItem
import eu.imeon.invaders.core.gameobjects.maze.MazeNode
import eu.imeon.invaders.core.gameobjects.maze.PacManMaze
import eu.imeon.invaders.core.gameobjects.maze.PillType
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer

class Pacman(
    val theme: Theme,
    private val gameEventLambda: GameEventLambda,
    private val pacManMaze: PacManMaze,
) : GameObject, Hitable, Controllable {

    private var paint = Paint().apply { color = Color.YELLOW }

    private var direction = Directions.LEFT
    private var nextDirection:Directions?=null
    private var moving=false
    private var currentNode=pacManMaze.pacmanHome

    private var alive = true

    private var speed = 5f
    private var t=0f
    private var tJaw=0f
    private var tPowerCountdown=0f

    private var logicalPos=PointF()
    private val radius=pacManMaze.nodeSize*.4f

    init {
        prepareGame()
    }

    override fun prepareLevel(difficultyLevel: Int) {
        alive = true
        direction = Directions.LEFT
        currentNode=pacManMaze.pacmanHome
        t=0f
        tJaw=0f
        tPowerCountdown=0f
    }

    override fun prepareGame() {
        GlobalGameState.lives = 3
        onControllerRestart()
    }

    override fun onControllerRestart() {
        alive = true
        direction = Directions.LEFT
        currentNode=pacManMaze.pacmanHome
    }

    private fun canWeGoThere(node: MazeNode, dir: Directions):Boolean {
        val targetNode= pacManMaze.getMazeNode(node.x+dir.dx, node.y+dir.dy) ?: return false

        if (targetNode.mazeItem.isWall)
            return false

        if (targetNode.mazeItem== MazeItem.GHOST_DOOR)
            return false

        return true
    }

    override fun update(deltaT: Float) {
        if (!alive) {
            return
        }
        tJaw+=deltaT
        if (tPowerCountdown>0f){
            tPowerCountdown=(tPowerCountdown-deltaT).coerceAtLeast(0f)
        }
        var readyForChange=false
        if (moving) {
            t += deltaT
            val tNorm = (t * speed).coerceIn(0f, 1f)
            val x = currentNode.x + direction.dx * tNorm
            val y = currentNode.y + direction.dy * tNorm
            logicalPos.set(x, y)
            if (tNorm >= 1f) {
                t = 0f
                // Target node reached
                val mx0 = currentNode.x + direction.dx
                val my0 = currentNode.y + direction.dy
                currentNode = pacManMaze.getMazeNode(mx0, my0)
                    ?: throw InternalError("Pacman ran into nowhere")
                // TODO: Extra GameEvent (Pacman-Event) for Pac-Man-Components intercommunication.
                // From here send POWER-PILL-EVENT to the ghosts and trigger
                // modification of game state.
                if (currentNode.pillState){
                    currentNode.pillState=false
                    if (currentNode.mazeItem.pillType==PillType.POWER_PILL){
                        tPowerCountdown=12f
                    }

                }
                if (!canWeGoThere(currentNode, direction)) {
                    moving = false
                }
                readyForChange=true
            }
        } else{
            logicalPos.set(currentNode.x.toFloat(), currentNode.y.toFloat())
            readyForChange=true
        }
        if (readyForChange){
            if (nextDirection!=null && canWeGoThere(currentNode, nextDirection!!)){
                direction=nextDirection!!
                nextDirection=null
                moving=true
            }

        }
    }


    override fun draw(canvas: Canvas) {


        if (!alive)
            return

        if (GlobalGameState.godMode)
            paint.alpha = ((System.currentTimeMillis() / 2L) % 256L).toInt()

        val pos=pacManMaze.logicalPos2VScreenPos(logicalPos)
//        val alpha=direction.index* PI.toFloat()*.5f
//        theme.player.drawRotated(canvas, paint, pos, alpha)
        val size=pacManMaze.nodeSize
        val vRect=RectF(pos.x, pos.y, pos.x+size, pos.y+size)
        val transformer = VScreen2CanvasTransformer(canvas)
        val cRect=transformer.toCanvas(vRect)
        val index=(direction.index+3)%4
        val t1=((tJaw*3f)%1f)*2f
        val t2= if (t1<=1f) t1 else 2f-t1
        val sweepRange=70f
        val beta=t2*sweepRange
        val alphaStart=sweepRange-beta+index*90f
        val sweepAngle=360f-sweepRange*2f+beta*2f
        paint.apply { color=if (hasSuperpower()) Color.WHITE else Color.YELLOW }
        canvas.drawArc(cRect, alphaStart, sweepAngle, true, paint)
    }

    override fun onControllerPan(dir: Int) {
        if (dir==0)
            return
        nextDirection=if (dir<0)  Directions.LEFT else Directions.RIGHT

    }

    override fun onControllerTilt(dir: Int) {
        if (dir==0)
            return
        nextDirection=if (dir<0)  Directions.UP else Directions.DOWN
    }

    override fun onControllerKill() {
        if (GlobalGameState.lives > 0)
            GlobalGameState.lives--

        val gameEvent = if (GlobalGameState.lives > 0)
            GameEvent.PlayerShipExplodes
        else
            GameEvent.PlayerDefeated

        val pos=pacManMaze.logicalPos2VScreenPos(logicalPos)
        gameEventLambda(gameEvent, pos, 1f,theme.player, 0)
        alive = false

    }

    override fun getHitRect(): RectF {
        val pos=pacManMaze.logicalPos2VScreenPos(logicalPos)
        val size=pacManMaze.nodeSize
        return RectF(pos.x, pos.y, pos.x+size, pos.y+size)
    }

    override fun onHit(source: Hitable): Boolean {
        if (GlobalGameState.godMode)
            return false

        if (source.performerType() != PerformerType.Aggressor)
            return false

        if (hasSuperpower())
            return false

        onControllerKill()
        return true
    }

    override fun performerType(): PerformerType {
        return PerformerType.Player
    }

    override fun registerHitables(superCollider: SuperCollider) {
        if (alive)
            superCollider.register(this)
    }

    override fun onControllerFire() {
    }

    override fun isWhere(): Point = Point(currentNode.x, currentNode.y)

    override fun hasSuperpower(): Boolean {
        return tPowerCountdown>0
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        superCollider.test(getHitRect()) { target: Hitable ->
            target.onHit(this)
        }
    }
}
