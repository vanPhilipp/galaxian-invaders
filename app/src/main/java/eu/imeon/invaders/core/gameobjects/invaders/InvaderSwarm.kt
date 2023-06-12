package eu.imeon.invaders.core.gameobjects.invaders

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.missiles.MissileManager
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.sound.SoundType
import eu.imeon.invaders.core.vscreen.VScreen
import kotlin.random.Random

class InvaderSwarm(
    val theme: Theme,
    val gameEventLambda: GameEventLambda,
    private val missileManager: MissileManager,
    private val soundPlayer: SoundPlayer
) : GameObject {

    private var invaders = mutableListOf<Invader>()

    private val sprites = theme.invaders
    private val invaderTypes = theme.invaderTypes

    private var paint = Paint().apply { color = Color.WHITE }

    private val speed = PointF(10f, 10f)
    private var direction = 1
    private var touchDown = false
    private var orgTargetY = 0f
    private var sinking = false
    private var peaceful = false
    private var lazy = false
    private var startCount = 0

    companion object {
        val swarmOrg = PointF(0f, 0f)

        var tickTock = false
            private set

        var fourQuarterBeat = 0
            private set

        var menaceIntervalMilli = 700
            private set
    }

    override fun prepareLevel(difficultyLevel: Int) {
        invaders.clear()
        touchDown = false
        val wavesNorm = ((difficultyLevel - 1) / 5f).coerceIn(0f, 1f)

        swarmOrg.set(0f, VScreen.vScaled(10f + wavesNorm * 25f))
        orgTargetY = swarmOrg.y
        sinking = false
        val columns = 11
        val rows = (4 + difficultyLevel / 3).coerceAtMost(7)
        val delta = theme.invaderGrid
        val org = PointF(delta.width, delta.height)
        for (k in 0 until rows) {
            for (i in 0 until columns) {
                val x = org.x + delta.width * i
                val y = org.y + delta.height * k
                val k1 = rows - 1 - k
                val i1 = columns - 1 - i
                val iNorm = (k1 * columns + i1).toFloat() / (columns * rows).toFloat()
                val delaySec = 1f + iNorm * 5f
                val fadeInSec = .7f
                val typeId = invaderTypes[k % invaderTypes.size]
                val sprite = sprites[typeId]
                val severity=(rows-k).toFloat()
                invaders.add(Invader(x, y, sprite, gameEventLambda, delaySec, fadeInSec,severity))
            }
        }
        startCount = invaders.size
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(255, 0, 255, 0)

        invaders.forEach { it.draw(canvas, paint) }
    }

    private fun singleInvaderAttackController(deltaT: Float, invader: Invader) {
        // Base probability
        val pMin = .01f
        val pMax = .3f
        val factor = (1f - invaders.size.toFloat() / startCount.toFloat()).coerceIn(0f, 1f)
        var p = pMin + (pMax - pMin) * factor


        // Probability is normalized to 1 second. To adjust to the interval length
        // we simply need to multiply with the interval length (given in seconds)
        p *= deltaT

        if (Random.nextFloat() > p)
            return

        val bulletOrg = invader.getFireOrg()
        val bulletSpeed = PointF(0f, 30f)
        missileManager.fireInvader(bulletOrg, bulletSpeed, invader)
        gameEventLambda(GameEvent.AlienShooting, bulletOrg, 1f, null, 0)

    }

    private fun invaderAttackController(deltaT: Float) {
        if (peaceful)
            return

        invaders.filter { it.active }.onEach {
            singleInvaderAttackController(deltaT, it)
        }
    }

    private fun updateSwarmMovement(deltaT: Float) {
        if (sinking) {
            val nextOrg = PointF(swarmOrg.x, swarmOrg.y + speed.y * deltaT)
            if (nextOrg.y > orgTargetY) {
                nextOrg.y = orgTargetY
                sinking = false
            }

            val touchDownInvader: Invader? = invaders.find { it.testTouchDown(swarmOrg) }

            if (!touchDown && touchDownInvader != null) {
                gameEventLambda(
                    GameEvent.AlienLanded,
                    touchDownInvader.getFireOrg(),
                    1f,
                    touchDownInvader.sprite,
                    touchDownInvader.getCurrentBitmapId()
                )
                touchDown = true
            }
            if (!touchDown)
                swarmOrg.set(nextOrg)
        } else {
            val speedX = if (lazy) speed.x * .1f else speed.x
            val nextOrg = PointF(swarmOrg.x + speedX * direction * deltaT, swarmOrg.y)
            var bump = 0

            for (i in invaders) {
                val b = i.testBumpLeftRight(direction, nextOrg)
                if (b != 0)
                    bump = b
            }
            if (bump != 0) {
                direction = bump
                orgTargetY += theme.invaderGrid.height
                sinking = true
            } else {
                swarmOrg.set(nextOrg)
            }
        }

    }

    override fun update(deltaT: Float) {
        updateSpeedSettings()

        if (GlobalGameState.currentState != GameState.WaveInProgress)
            return

        if (touchDown)
            return

        updateSwarmMovement(deltaT)
        invaderAttackController(deltaT)

        invaders
            .forEach { it.update(deltaT) }

        val sizeOld = invaders.size

        invaders = invaders
            .filter { it.alive }
            .toMutableList()

        if (invaders.size == 0 && sizeOld > 0) {
            gameEventLambda(GameEvent.WaveCompleted, PointF(), 1f, null, 0)
        }
    }

    fun cheatCodeSinkFast() {
        orgTargetY += theme.invaderGrid.height * 20f
        sinking = true
        Log.i("debug", "Sink fast!")
    }

    fun cheatCodePeace() {
        peaceful = !peaceful
        Log.i("debug", "Peacful: $peaceful")
    }

    fun cheatCodeLazy() {
        lazy = !lazy
        Log.i("debug", "Lazy: $lazy")
    }

    fun cheatCodeAutoKill() {
        Log.i("debug", "Autokill!")
        val target: Invader = invaders.firstOrNull { it.active } ?: return
        target.destroy()
    }

    override fun prepareGame() {
    }

    private fun updateFourQuarter(beat: Int) {
        soundPlayer.play(
            when (beat) {
                0 -> SoundType.Invader1
                1 -> SoundType.Invader2
                2 -> SoundType.Invader3
                3 -> SoundType.Invader4
                else -> SoundType.Invader1
            }
        )
    }

    private fun <T> executeOnChange(old: T, new: T, onChange: (T) -> Unit): T {
        if (old != new)
            onChange(new)
        return new
    }

    private fun updateSpeedSettings() {
        if (startCount == 0)
            return


        val speedMin = (1f + .1f * GlobalGameState.waves).coerceIn(1f, 2f)
        val speedMax = (20f + 1f * GlobalGameState.waves).coerceIn(1f, 100f)
        val f0 = invaders.size.toFloat() / startCount.toFloat()
        val f1 = 1f - f0
        val s = f0 * speedMin + f1 * speedMax
        speed.set(s, s)

        menaceIntervalMilli = (7000f / speed.x).toInt().coerceAtLeast(1)
        tickTock = executeOnChange(
            tickTock,
            (System.currentTimeMillis() / menaceIntervalMilli) % 2L != 0L
        ) {}

        fourQuarterBeat = executeOnChange(
            fourQuarterBeat,
            ((System.currentTimeMillis() / (menaceIntervalMilli / 2L)) % 4L).toInt()
        ) {
            if (GlobalGameState.currentState == GameState.WaveInProgress)
                updateFourQuarter(it)
        }

    }

    override fun registerHitables(superCollider: SuperCollider) {
        invaders.forEach {
            if (it.alive)
                superCollider.register(it)
        }
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        invaders.forEach {
            superCollider.test(it.getHitRect()) { target: Hitable ->
                target.onHit(it)
            }
        }
    }

    override fun readyForRespawn(superCollider: SuperCollider): Boolean {
        return true
    }
}