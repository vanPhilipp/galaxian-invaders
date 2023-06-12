package eu.imeon.invaders.core.gameobjects.mystery

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
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.sound.SoundType
import eu.imeon.invaders.core.vscreen.VScreen
import kotlin.random.Random

class MysteryUfo(
    theme: Theme,
    val gameEventLambda: GameEventLambda,
    val soundPlayer: SoundPlayer
) : GameObject, Hitable {

    private val sprite = theme.mysteryUfo

    private val size = sprite.size
    private val pos = PointF()
    private val speedX = VScreen.hScaled(10f)
    private var direction = 0
    private val startXLeft = -size.width
    private val startXRight = VScreen.width
    private val startY = VScreen.vScaled(VScreen.height*.04f)
    private var tTenSeconds = 0
    private var t = 0f
    private var soundActive = false
    private var paint = Paint().apply { color = Color.WHITE }

    private var hitCount=0

    private var alive = false

    init {
        prepareGame()
    }

    override fun prepareLevel(difficultyLevel: Int) {
        alive = false
        direction = 0
    }

    override fun prepareGame() {
        hitCount=0
    }

    private fun randomStartMysteryUFO() {
        val tTenSecondsNew = t.toInt() / 10
        if (tTenSecondsNew != tTenSeconds) {
            tTenSeconds = tTenSecondsNew
            if (Random.nextFloat() > .5f)
                start()
        }
    }

    private fun manageSound() {
        val active=  (GlobalGameState.currentState == GameState.WaveInProgress) && alive

        if (soundActive != active) {
            soundActive = active
            if (active)
                soundPlayer.play(SoundType.MysteryUFO)
            else
                soundPlayer.stop(SoundType.MysteryUFO)
        }

    }

    override fun update(deltaT: Float) {
        manageSound()
        if (GlobalGameState.currentState != GameState.WaveInProgress)
            return

        t += deltaT

        randomStartMysteryUFO()

        if (!alive) {
            return
        }

        val x = pos.x + direction * speedX * deltaT
        if ((direction > 0 && x > VScreen.width) || (direction < 0 && x < -size.width)) {
            alive = false
            return
        }
        pos.x = x


    }

    private fun getCenterPoint(): PointF {
        return PointF(pos.x + size.width / 2, pos.y + size.height / 2)
    }

    override fun draw(canvas: Canvas) {
        if (!alive)
            return

        paint.color = Color.WHITE
        sprite.draw(canvas, paint, pos)
    }

    fun start() {
        if (alive)
            return

        alive = true
        direction = if (Random.nextFloat() > .5f) {
            pos.set(startXRight, startY)
            -1
        } else {
            pos.set(startXLeft, startY)
            1
        }

    }

    override fun getHitRect(): RectF {
        return RectF(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
    }

    override fun onHit(source: Hitable): Boolean {
        if (source.performerType() != PerformerType.Player)
            return false

        alive = false
        hitCount++
        gameEventLambda(GameEvent.MysteryShipExplodes, getCenterPoint(), 1f, sprite,0)
        return true
    }

    override fun performerType(): PerformerType {
        return PerformerType.Aggressor
    }

    override fun registerHitables(superCollider: SuperCollider) {
        if (!alive)
            return

        superCollider.register(this)
    }
}
