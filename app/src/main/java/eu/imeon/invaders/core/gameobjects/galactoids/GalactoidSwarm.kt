package eu.imeon.invaders.core.gameobjects.galactoids

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.missiles.MissileManager
import eu.imeon.invaders.core.gameobjects.particles.ParticleEngine
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.vscreen.VScreen
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random


class GalactoidSwarm(
    theme: Theme,
    val gameEventLambda: GameEventLambda,
    val missileManager: MissileManager,
    val particleEngine: ParticleEngine
) : GameObject {

    private val paint = Paint()
    private val sprites = theme.invaders

    // All Galactoids are managed in a virtual two dimensional array
    private var galactoids = mutableMapOf<Int, Galactoid>()
    private var startCount = 0
    var peaceful = false

    companion object {
        val size = Point(0, 0)
        private val swarmOrigin = PointF(VScreen.centerX, VScreen.height * .4f)
        private val radiusMax = VScreen.width * .2f
        private val radiusMin = radiusMax * .9f
        private const val pumpSpeed = 0.2 * PI
        var swarmTime = 0f
        var nextAttackAt = 10f

        private const val keyFactor = 1000
        private fun logicalPos2key(x: Int, y: Int): Int = x + y * keyFactor
        private fun key2LogicalPos(key: Int): Point {
            val x = key % keyFactor
            val y = key / keyFactor
            return Point(x, y)
        }

        private fun isRightWing(key: Int): Boolean {
            return (key % keyFactor) >= size.x / 2
        }

        fun calcSwarmPos(key: Int, t: Float): PointF {
            val logicalPos = key2LogicalPos(key)
            val x0 = (2f / size.x.toFloat() * logicalPos.x.toFloat() - 1f) * radiusMin
            val y0 = (2f / size.y.toFloat() * logicalPos.y.toFloat() - 1f) * radiusMin
            val range = 0.5f * ((radiusMax / radiusMin) - 1f)
            val factor = 1f + range * sin(t * pumpSpeed).toFloat()
            return PointF(
                swarmOrigin.x + x0 * factor,
                swarmOrigin.y + y0 * factor
            )
        }

        fun calcSwarmPos(key: Int): PointF {
            return calcSwarmPos(key, swarmTime)
        }
    }

    override fun prepareLevel(difficultyLevel: Int) {
        galactoids.clear()
        val rows = (4+difficultyLevel/4).coerceAtMost(7)
        val columns = (5+difficultyLevel).coerceAtMost(7)
        size.set(columns, rows)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val spriteIdx = (y / 2).coerceAtMost(sprites.size - 1)
                val key = logicalPos2key(x, y)
                val index = y * rows + x
                val count = rows * columns
                val startDelay = index.toFloat() / count.toFloat() * 12f + y * 1.5f
                val flightLength = 3f
                galactoids[key] = Galactoid(
                    key,
                    sprites[spriteIdx],
                    startDelay,
                    flightLength,
                    gameEventLambda,
                    missileManager,
                    particleEngine,
                    (y and 1)!=0
                )
            }
        }
        startCount = galactoids.size
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(255, 0, 255, 0)

        galactoids.forEach { it.value.draw(canvas, paint) }
    }

    override fun drawDebug(canvas: Canvas) {
        paint.color = Color.argb(255, 0, 255, 0)

        galactoids.forEach { it.value.drawDebug(canvas, paint) }

    }

    override fun update(deltaT: Float) {
        swarmTime += deltaT
        val oldNotEmpty = galactoids.isNotEmpty()
        galactoids = galactoids
            .onEach {
                val (_, galactoid) = it
                galactoid.update(deltaT)
            }
            .filter {
                it.value.active
            }
            .toMutableMap()

        invaderAttackController()
        if (galactoids.isEmpty() && oldNotEmpty) {
            gameEventLambda(GameEvent.WaveCompleted, PointF(), 1f, null, 0)
        }
    }


    override fun prepareGame() {
    }

    fun autoKillNext() {
        val target = galactoids.values.firstOrNull { it.active } ?: return
        target.active = false
        gameEventLambda(GameEvent.AlienExplodes, target.pos, 1f, target.sprite, 0)

    }

    private fun initClusteredAttack(galactoid: Galactoid, flightTime: Float, deltas: Array<Point>) {
        val mirrored = isRightWing(galactoid.key)
        val patternId = Random.nextInt(0, 7)
        galactoid.attack(flightTime, galactoid.pos, patternId, mirrored)
        val lp = key2LogicalPos(galactoid.key)

        deltas.forEach { delta ->
            logicalPos2key(lp.x + delta.x, lp.y + delta.y).let {
                if (galactoids.containsKey(it))
                    galactoids[it]?.attack(flightTime, galactoid.pos, patternId, mirrored)
            }
        }
    }

    private fun clusterFormationFactory(): Array<Point> {
        val deltas = when (Random.nextInt(0, 4)) {
            0 -> arrayOf(
                Point(1, 0),
                Point(0, 1)
            )

            1 -> arrayOf(
                Point(-1, 0),
                Point(0, 1)
            )

            2 -> arrayOf(
                Point(-1, 0),
                Point(1, 0),
                Point(0, 1),
                Point(0, -1)
            )

            else -> arrayOf(
                Point(-1, 0),
                Point(1, 0)
            )
        }
        return deltas
    }

    fun triggerAttack() {
        val candidates = galactoids.values.filter { it.active && !it.flightInProgress }
        if (candidates.isEmpty())
            return

        val index = Random.nextInt(0, candidates.size)
        val deltas = clusterFormationFactory()

        initClusteredAttack(candidates[index], 15f, deltas)
    }

    private fun invaderAttackController() {
        if (peaceful)
            return

        if (GlobalGameState.currentState != GameState.WaveInProgress)
            return

        if (swarmTime >= nextAttackAt) {
            nextAttackAt = swarmTime + .5f + Random.nextFloat() * 2f
            triggerAttack()
        }
    }


    override fun registerHitables(superCollider: SuperCollider) {
        galactoids.values.forEach {
            if (it.active)
                superCollider.register(it)
        }
    }

    override fun checkCollisions(superCollider: SuperCollider) {
        galactoids.values.forEach {
            superCollider.test(it.getHitRect()) { target: Hitable ->
                target.onHit(it)
            }
        }
    }

    override fun readyForRespawn(superCollider: SuperCollider): Boolean {
        var inFlightCount = 0
        galactoids.onEach {
            val (_, galactoid) = it
            if (galactoid.flightInProgress)
                inFlightCount++
        }
        return inFlightCount == 0
    }
}