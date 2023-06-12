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
import eu.imeon.invaders.core.gameobjects.particles.ParticleEngine
import eu.imeon.invaders.core.gameobjects.particles.SimpleParticle
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.sound.SoundType
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.resize
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random

class RotatingShip(
    theme: Theme,
    private val gameEventLambda: GameEventLambda,
    private val missileManager: MissileManager,
    private val soundPlayer: SoundPlayer,
    private val particleEngine: ParticleEngine
) : GameObject, Hitable, Controllable {

    private val sprite = theme.player

    private var paint = Paint().apply { color = Color.WHITE }
    private val size = sprite.size

    private val startX = VScreen.width / 2
    private val startY = VScreen.height / 2
    private val pos = PointF(startX, startY)
    private var alpha = 0f

    private val rotationSpeed = PI.toFloat()
    private var direction = 0

    private var alive = true

    private var speed = PointF()
    private var speedUp = 10f
    private var speedDown = 1f
    private var speedMax = (VScreen.width + VScreen.height) / 2
    private var thrustActive = false

    private var particleCounter = 0
    private var particleTime = 0f

    init {
        prepareGame()
    }

    override fun prepareLevel(difficultyLevel: Int) {
        alive = true
        direction = 0
        thrustActive=false
    }

    override fun prepareGame() {
        GlobalGameState.lives = 3
        onControllerRestart()
    }

    override fun onControllerRestart() {
        pos.set(PointF(startX, startY))
        speed.set(0f, 0f)
        alive = true
        thrustActive=false
    }

    private fun modulo(v: Float, range: Float): Float {
        val result = v % range
        return if (result >= 0) result else range + result
    }

    private fun decelerate(v: Float, deltaSpeed: Float): Float {
        val s = sign(v)
        val va = abs(v)
        return s * (va - deltaSpeed).coerceAtLeast(0f)

    }

    private fun toDegree(rad:Float):Float= rad/ PI.toFloat()*180f

    private fun updateThrustParticles(deltaT: Float) {
        particleTime += deltaT
        val counter = (particleTime * 100f).toInt()
        if (counter == particleCounter)
            return

        val delta = counter - particleCounter
        particleCounter = counter

        if (!thrustActive)
            return

        for (i in 0 until delta) {

            val spread=.3f
            val alpha1 = alpha + PI.toFloat() / 2f - spread + 2f*spread* Random.nextFloat()
            val speed=VScreen.width * (1f+Random.nextFloat()*.1f)
            val lifetime=.2f + Random.nextFloat()*.2f
            particleEngine.add(
                SimpleParticle(
                    pos.x,
                    pos.y,
                    VScreen.width * .01f,
                    toDegree(alpha1),
                    speed,
                    lifetime,
                    Color.WHITE
                )
            )
        }


    }


    override fun update(deltaT: Float) {
        if (!alive) {
            return
        }

        updateThrustParticles(deltaT)

        alpha = (alpha + direction * rotationSpeed * deltaT)
        if (alpha < 0f)
            alpha += (2f * PI).toFloat()

        val alpha1 = alpha - PI.toFloat() / 2f

        speed.x = decelerate(speed.x, speedDown * deltaT)
        speed.y = decelerate(speed.y, speedDown * deltaT)

        if (thrustActive) {
            speed.x = (speed.x + cos(alpha1) * speedUp * deltaT).coerceIn(-speedMax, speedMax)
            speed.y = (speed.y + sin(alpha1) * speedUp * deltaT).coerceIn(-speedMax, speedMax)
        }


        speed.x =
            (speed.x + if (thrustActive) cos(alpha1) * speedUp * deltaT else -speedDown * deltaT).coerceIn(
                -speedMax,
                speedMax
            )
        speed.y =
            (speed.y + if (thrustActive) sin(alpha1) * speedUp * deltaT else -speedDown * deltaT).coerceIn(
                -speedMax,
                speedMax
            )
        val x = modulo(pos.x + speed.x * deltaT, VScreen.width)
        val y = modulo(pos.y + speed.y * deltaT, VScreen.height)
        pos.set(x, y)

    }


    override fun draw(canvas: Canvas) {

        paint.color = Color.WHITE

        if (!alive)
            return

        paint.color = Color.GREEN
        if (GlobalGameState.godMode)
            paint.alpha = ((System.currentTimeMillis() / 2L) % 256L).toInt()
        sprite.drawRotated(canvas, paint, pos, alpha)
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
            val bulletStart = PointF(pos.x, pos.y)
            val bulletSpeed = 100f
            val beta = alpha - PI.toFloat() / 2f
            val bulletDir = PointF(cos(beta) * bulletSpeed, sin(beta) * bulletSpeed)
            missileManager.firePlayer(bulletStart, bulletDir, this, alpha)
        }

    }

    override fun onControllerKill() {
        if (GlobalGameState.lives > 0)
            GlobalGameState.lives--

        val gameEvent = if (GlobalGameState.lives > 0)
            GameEvent.PlayerShipExplodes
        else
            GameEvent.PlayerDefeated

        gameEventLambda(gameEvent, pos, 1f, sprite, 0)
        alive = false

    }

    override fun getHitRect(): RectF {
        val hx=size.width/2f
        val hy=size.height/2f

        return RectF(pos.x-hx, pos.y-hy, pos.x + hx, pos.y +hy).resize(.7f)
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

    override fun onControllerThrust(set: Boolean) {
        thrustActive = set
    }
}
