package eu.imeon.invaders.core.gameobjects.galactoids

import android.graphics.*
import androidx.core.graphics.minus
import dev.benedikt.math.bezier.spline.DoubleBezierSpline
import dev.benedikt.math.bezier.vector.Vector2D
import eu.imeon.invaders.core.collision.Hitable
import eu.imeon.invaders.core.collision.PerformerType
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.gameobjects.missiles.MissileManager
import eu.imeon.invaders.core.gameobjects.particles.ParticleEngine
import eu.imeon.invaders.core.gameobjects.particles.SimpleParticle
import eu.imeon.invaders.core.gamestate.GameState
import eu.imeon.invaders.core.gamestate.GlobalGameState
import eu.imeon.invaders.core.util.SmoothCurveGenerator
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer
import eu.imeon.invaders.core.vscreen.resize
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class Galactoid(
    val key: Int,
    val sprite: Sprite,
    startDelay: Float,
    private var flightLength: Float,
    private val gameEventLambda: GameEventLambda,
    val missileManager: MissileManager,
    private val particleEngine: ParticleEngine,
    private val mirrorX:Boolean
) : Hitable {
    var active = true
    private var alpha = 0f
    val pos = PointF()
    private val tangent=PointF()
    var flightInProgress = true
    private var flightTime = -startDelay
    private var spline = DoubleBezierSpline<Vector2D>()
    private val smoothCurveGenerator = SmoothCurveGenerator(.1, .1, 1.0)
    private val size = sprite.size
    private val missileShootTimes = ArrayList<Float>()

    private var particleIndex = 0
    private val particlesPerSecond = 20
    private val path = Path()

    init {
        val t1 = GalactoidSwarm.swarmTime + startDelay + flightLength
        val targetPos = GalactoidSwarm.calcSwarmPos(key, t1)

        val points= arrayOf<Vector2D>(
            Vector2D(VScreen.width / 2.0, -VScreen.height * .1),
            Vector2D(VScreen.width / 5.0, VScreen.height * .7),
        )
        if (mirrorX){
            points.onEach { it.x=VScreen.width-it.x }
        }
        spline.addKnots(
            *points, //Vector2D(VScreen.width/4.0*3.0, VScreen.height*.5),
            Vector2D(targetPos.x.toDouble(), targetPos.y.toDouble())
        )
        spline.compute()
    }

    private fun createVector2D(pos: PointF): Vector2D =
        Vector2D((VScreen.width * pos.x), (VScreen.height * pos.y))

    private fun interpolate(a: Float, b: Float, p: Float): Float = a + (b - a) * p

    private fun updateWhenFlightInProgress() {
        // Local time is coerced to (0..1) range and smoothed.

        //particleEmitter()

        val tCoerced = (flightTime / flightLength).coerceIn(0f, 1f)
        val tLocal = smoothCurveGenerator.calc(tCoerced)

        // Transform current spline into position and direction

        val p = spline.getCoordinatesAt(tLocal.toDouble())
        val t = spline.getTangentAt(tLocal.toDouble())
        val halfPi = PI.toFloat() / 2f
        val alpha0 = atan2(tangent.y, tangent.x).toFloat() + halfPi

        // Ensure invader is upright when close to target

//        val tFade = if (tLocal >= .9f) (tLocal - .9f) / .1f else 0f
//        alpha = interpolate(alpha0, 0f, tFade)

        alpha = if (tLocal >= 0.9)
            interpolate(alpha0, 0f, (tLocal - .9f) / .1f)
        else
            alpha0

        // Set final position

        pos.set(p.x.toFloat(), p.y.toFloat())
        tangent.set(t.x.toFloat(), t.y.toFloat())

        // Shooting
        missileLauncher()

        // Check if current flight is over.
        if (tCoerced >= 1f)
            flightInProgress = false

    }

    private fun missileLauncher() {
        if (missileShootTimes.size > 0) {
            val tNext = missileShootTimes.first()
            if (flightTime >= tNext) {
                missileShootTimes.removeFirst()

                if (GlobalGameState.currentState!=GameState.WaveInProgress)
                    return

                val bulletOrg = getFireOrg()
                val r=PointF(.5f+.5f*Random.nextFloat(), .5f+.5f*Random.nextFloat())
                val bulletSpeed = PointF(tangent.x*10f*r.x, 50f*r.y)
                missileManager.fireInvader(bulletOrg, bulletSpeed, this)
                gameEventLambda(GameEvent.AlienShooting, bulletOrg, 1f, null, 0)

            }
        }
    }

    fun update(deltaT: Float) {
        flightTime += deltaT
        if (!flightInProgress) {
            val swarmPos = GalactoidSwarm.calcSwarmPos(key)
            pos.set(swarmPos.x, swarmPos.y)
            return
        }
        updateWhenFlightInProgress()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (active)
            sprite.drawRotated(canvas, paint, pos, alpha)
    }
    fun drawDebug(canvas: Canvas, paint: Paint) {
        pathDebugger(canvas, paint)
    }

    private fun startFlight(s: DoubleBezierSpline<Vector2D>) {
        spline = s
        flightTime = 0f
        flightInProgress = true
        particleIndex = 0
    }

    private fun attackSplineFactory(
        deltaPos: PointF,
        endPos: PointF,
        mirrored: Boolean = false,
        medium0: List<PointF>
    ): DoubleBezierSpline<Vector2D> {

        val spline = DoubleBezierSpline<Vector2D>()

        val hFactor = if (mirrored) 1f else -1f

        // Create a begin loop that must start vertically. Otherwise it would not look smooth

        val launchNodes = arrayOf(
            Vector2D(0f, 0f),
            Vector2D(0f, -VScreen.vScaled(5f)),
            Vector2D(VScreen.hScaled(5f) * hFactor, -VScreen.vScaled(10f)),
        ).map {
            it + Vector2D.fromPointF(pos)
        }.toTypedArray()

        // In the medium part follow the given point list, scaled to VScreen coordinates.

        val mediumNodes = (if (mirrored) {
            medium0.map { PointF(1f - it.x, it.y) }
        } else medium0)
            // Add displacement bias form swarm flight
            .map {
                createVector2D(it) + Vector2D.fromPointF(deltaPos)
            }.toTypedArray()

        // Collect all nodes in one list

        val rawList = arrayOf<Vector2D>(
            *launchNodes,
            *mediumNodes,
            Vector2D.fromPointF(endPos)
        )

        // Sanitize the list: Nodes wich are too close to each other must be removed.
        // Otherwise the DoubleBezierSpline might crash

        var prev: Vector2D? = null
        val cleanList = arrayListOf<Vector2D>()
        for (current in rawList) {
            if (prev == null) {
                cleanList.add(current)
                prev = current
                continue
            }
            val delta = (current - prev)
            val length = sqrt(delta.x.pow(2.0) + delta.y.pow(2.0))
            prev = current
            if (length > 1e-4)
                cleanList.add(current)
        }
        spline.addKnots(*(cleanList).toTypedArray())
        spline.compute()
        return spline
    }

    fun attack(length: Float, refPos: PointF, patternId:Int, mirrored: Boolean) {
        if (flightInProgress)
            return

        flightLength = length
        val t1 = GalactoidSwarm.swarmTime + flightLength
        val endPos = GalactoidSwarm.calcSwarmPos(key, t1)
        val deltaPos = pos - refPos

        val medium0 = attackPatternFactory(patternId)

        startFlight(attackSplineFactory(deltaPos, endPos, mirrored, medium0))

        missileShootTimes.clear()
        missileShootTimes.apply {
            val t0=length*(Random.nextFloat()*.2f + 0.4f)
            val shootDelay=length*.05f
            var t = t0
            val count=Random.nextInt(2,6)
            for (i in 0..count) {
                t += shootDelay
                add(t)
            }
        }
    }

    private fun attackPatternFactory(patternId:Int): List<PointF> =
        when (patternId) {

            0 -> listOf(
                PointF(.1f, .6f),
                PointF(.9f, .7f),
                PointF(.1f, .8f),
                PointF(.9f, .9f),
                PointF(1f, .5f)
            )
            1 -> listOf(
                PointF(.2f, .6f),
                PointF(.8f, .7f),
                PointF(.4f, .8f),
                PointF(.6f, .9f)
            )
            2 -> listOf(
                PointF(.1f, .1f),
                PointF(.9f, .1f),
                PointF(.9f, .9f),
                PointF(.9f, .1f)
            )
            3 -> listOf(
                PointF(.2f, .1f),
                PointF(.4f, .7f),
                PointF(.6f, .1f),
                PointF(.8f, .7f)
            )
            4 -> listOf(
                PointF(.2f, .5f),
                PointF(.4f, .6f),
                PointF(.2f, .7f),
                PointF(.4f, .8f),
                PointF(.2f, .9f),
                PointF(.4f, .1f)
            )
            5 -> listOf(
                PointF(.1f, .6f),
                PointF(.9f, .9f),
                PointF(.1f, .9f),
                PointF(.1f, .1f),
                PointF(.9f, .1f)
            )
            else -> listOf(
                PointF(.1f, .6f),
                PointF(.3f, .7f),
                PointF(.5f, .6f),
                PointF(.7f, .7f),
                PointF(.9f, .6f)
            )


        }

    private fun boundingRect(): RectF {
        val hx=size.width/2f
        val hy=size.height/2f

        return RectF(pos.x-hx, pos.y-hy, pos.x + hx, pos.y +hy)
    }

    private fun getFireOrg(): PointF {
        return PointF(pos.x, pos.y + size.height/2)
    }

    override fun getHitRect(): RectF {
        return boundingRect()
    }

    override fun onHit(source: Hitable): Boolean {
        if (source.performerType() != PerformerType.Player)
            return false

        destroy()
        return true
    }


    fun destroy() {
        gameEventLambda(GameEvent.AlienExplodes, pos, 1f, sprite, 0)
        active=false
    }

    override fun performerType(): PerformerType {
        return PerformerType.Aggressor
    }


    private fun particleEmitter() {
        val index = (flightTime * particlesPerSecond).toInt()
        if (index > particleIndex) {
            particleIndex = index
            val center = PointF(pos.x + size.width / 2f, pos.y + size.height / 2f)
            particleEngine.add(
                SimpleParticle(
                    center.x, center.y, .5f, alpha * 180f / PI.toFloat(), 0f, 2f, Color.WHITE
                )
            )
        }
    }

    //  Debugging
    // =========

    private fun pathDebugger(canvas: Canvas, paint: Paint)  {
        if (!flightInProgress)
            return

        val count = 100
        val transformer = VScreen2CanvasTransformer(canvas)
        path.reset()
        for (i in 0 until count) {
            val t=i.toDouble()/(count-1).toDouble()
            val v2d=spline.getCoordinatesAt(t)
            val p1=transformer.toCanvas(PointF(v2d.x.toFloat(), v2d.y.toFloat()))
            if (i==0)
                path.moveTo(p1.x, p1.y)
            else
                path.lineTo(p1.x,p1.y)
        }
        paint.apply{
            style=Paint.Style.STROKE
            color=Color.MAGENTA
            strokeWidth
        }
        canvas.drawPath(path, paint)
    }


}

