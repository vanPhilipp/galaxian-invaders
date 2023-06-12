package eu.imeon.invaders.core.gameobjects.playership

import android.graphics.*
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.gameobjects.missiles.MissileManager
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.sound.SoundType
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.resize

class PlayerShip(
    theme: Theme,
    private val gameEventLambda: GameEventLambda,
    private val missileManager: MissileManager,
    private val soundPlayer: SoundPlayer,
) : GameObject, Hitable,Controllable {

    private val sprite = theme.player

    private var paint = Paint().apply { color = Color.WHITE }
    private val size = sprite.size

    private val startX = VScreen.width / 2 - size.width / 2
    private val startY = VScreen.invaderTouchDownLevel - size.height
    private val pos = PointF(startX, startY)

    private val speedX = VScreen.hScaled(40f)
    private var direction = 0

    private var alive = true

    init {
        prepareGame()
    }

    override fun prepareLevel(difficultyLevel: Int) {
        alive = true
        direction = 0

    }

    override fun prepareGame() {
        GlobalGameState.lives = 3
        onControllerRestart()
    }

    override fun onControllerRestart() {
        pos.set(PointF(startX, startY))
        alive = true
    }

    override fun update(deltaT: Float) {
        if (!alive) {
            return
        }

        val max = VScreen.width - size.width

        var x = pos.x + direction * speedX * deltaT
        if (x < 0f)
            x = 0f
        if (x > max)
            x = max
        pos.x = x
    }


    private fun getBulletStartPosition(): PointF {
        return PointF(pos.x + size.width / 2, pos.y)
    }

    override fun draw(canvas: Canvas) {

        paint.color = Color.WHITE
//        for (i in 0 until lives - 1) {
//            PointF(10f + size.width * 1.3f * i, VScreen.height - size.height * 1f).let {
//                sprite.draw(canvas, paint, it)
//            }
//        }

        if (!alive)
            return

        paint.color = Color.GREEN
        if (GlobalGameState.godMode)
            paint.alpha = ((System.currentTimeMillis() / 2L) % 256L).toInt()
        sprite.draw(canvas, paint, pos)

    }

    override fun onControllerPan(dir: Int) {
        if (GlobalGameState.currentState == GameState.GameOver)
            return
        direction = dir
    }

    override fun onControllerFire() {
        if (GlobalGameState.currentState == GameState.GameOver)
            return
        if (!alive)
            return

        if (soundPlayer.play(SoundType.Shoot)) {
            val bulletStart = getBulletStartPosition()
            missileManager.firePlayer(bulletStart, PointF(0f, -150f), this, 0f)
        }

    }

    override fun onControllerKill() {
        if (GlobalGameState.lives > 0)
            GlobalGameState.lives--

        val gameEvent = if (GlobalGameState.lives > 0)
            GameEvent.PlayerShipExplodes
        else
            GameEvent.PlayerDefeated

        gameEventLambda(gameEvent, pos, 1f, sprite,0)
        alive = false

    }

    override fun getHitRect(): RectF {
        return RectF(pos.x, pos.y, pos.x + size.width, pos.y + size.height).resize(.5f)
    }

    override fun onHit(source: Hitable): Boolean {
        if (GlobalGameState.godMode)
            return false

        if (source.performerType() != PerformerType.Aggressor)
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
}
