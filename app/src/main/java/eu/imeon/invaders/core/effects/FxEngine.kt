package eu.imeon.invaders.core.effects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.alpha
import androidx.core.graphics.plus
import eu.imeon.invaders.core.gameobjects.particles.GrowingParticle
import eu.imeon.invaders.core.gameobjects.particles.PairF
import eu.imeon.invaders.core.gameobjects.particles.ParticleEngine
import eu.imeon.invaders.core.gameobjects.particles.Photon
import eu.imeon.invaders.core.gameobjects.particles.SimpleParticle
import eu.imeon.invaders.core.sound.SoundPlayer
import eu.imeon.invaders.core.sound.SoundType
import eu.imeon.invaders.core.util.Sprite
import eu.imeon.invaders.core.vscreen.VScreen
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random


typealias GameEventLambda = (gameEvent: GameEvent, pos: PointF, severity: Float, sprite: Sprite?, textureId: Int) -> Unit

class FxEngine(private val particleEngine: ParticleEngine, private val soundPlayer: SoundPlayer) {

    fun createFx(
        gameEvent: GameEvent,
        pos: PointF,
        severity: Float,
        sprite: Sprite?,
        textureId: Int
    ) {
        when (gameEvent) {
            GameEvent.MysteryShipExplodes,
            GameEvent.AlienExplodes
            -> onFxAlienExplodes(
                pos,
                sprite,
                textureId
            )
            GameEvent.AsteroidExplodes
            -> onFxAsteroidExplodes(
                pos,severity
            )
            GameEvent.ShelterDamaged
            -> onFxShelterDamaged(pos, severity)

            GameEvent.PlayerDefeated, GameEvent.PlayerShipExplodes
            -> onFxPlayerShipExplodes(
                pos,
                severity,
                sprite,
                textureId
            )
            GameEvent.AlienLanded
            -> onFxAlienLanded(pos, severity)

            GameEvent.WaveCompleted
            -> onFxWaveCompleted(pos, severity)

            GameEvent.GameOver
            -> onFxGameOver(pos, severity)

            GameEvent.AlienShooting
            -> onFxAlienShooting(pos, severity)

            GameEvent.StartNewGame -> {}
        }
    }


    private fun renderSpriteIntoParticles(
        sprite: Sprite?,
        textureId: Int,
        pos: PointF,
        lifetime0: Float = .9f,
        growthFactor: Float = 4f
    ) {
        if (sprite == null) {
            Log.e(this::class.simpleName, "Sprite missing")
            return
        }
        val bmp:Bitmap = sprite.getScaledBitmap(textureId) ?: return
        val alphaDelta = -90f + Random.nextFloat() * 180f
        val count = 16
        for (nx in 0 until count) {
            for (ny in 0 until count) {
                val x = (nx * bmp.width) / count
                val y = (ny * bmp.height) / count
                val pixel = bmp.getPixel(x, y)
                if (pixel.alpha > 32) {

                    val dx = x - bmp.width / 2
                    val dy = y - bmp.height / 2
                    val alpha = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                        .toFloat() //+ Random.nextFloat() * 5f - 2.5f
                    val radius = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    //val alpha = Random.nextFloat() * 360f
                    val lifetime = lifetime0 + Random.nextFloat() * .1f
                    val radiusMax = radius * sprite.vPixelSize() * growthFactor
                    val particleSize0 = sprite.size.width / count
                    val particleSize1 = sprite.size.width / count * growthFactor

                    val alphaSpread = -30f + Random.nextFloat() * 60f
                    particleEngine.add(
                        GrowingParticle(
                            pos.x + x * sprite.vPixelSize(),
                            pos.y + y * sprite.vPixelSize(),
                            particleSize0,
                            particleSize1,
                            alpha,
                            alpha + alphaDelta + alphaSpread,
                            radiusMax,
                            lifetime,
                            pixel,
                            VScreen.height * .1f
                        )
                    )
                }
            }
        }
    }

    private fun bitmapDisassembler(
        bmp: Bitmap,
        origin: PointF,
        radiusScale: Float,
        rectExtend: PairF,
        lifetimeRange: PairF,
        delaySec: Float,
        speed0:Float,
        proportionalSpeed:Boolean
    ) {

        val w2 = bmp.width.toFloat() / 2f
        val h2 = bmp.height.toFloat() / 2f
        val rMax= sqrt(w2*w2+h2*h2)
        for (y in 0 until bmp.height) {
            for (x in 0 until bmp.width) {
                val color = bmp.getPixel(x, y)
                if (color.alpha > 32) {
                    val dx = (x - w2)
                    val dy = (y - h2)
                    val direction = atan2(dy, dx)
                    val r= sqrt(dx*dx+dy*dy)
                    val speed=if (proportionalSpeed) r/rMax*speed0 else speed0

                    val lifetime=lifetimeRange.first+Random.nextFloat()*(lifetimeRange.second-lifetimeRange.first)
                    particleEngine.add(
                        Photon(
                            origin + PointF(dx * radiusScale, dy * radiusScale),
                            PairF(direction, direction),// + PI.toFloat() / 2f),
                            rectExtend,
                            speed,
                            lifetime,
                            color,
                            true,
                            delaySec

                        )
                    )
                }
            }
        }
    }

    // Set paint.color, textSize and textType
    private fun textAsBitmap(text: String, paint: Paint): Bitmap {
        paint.textAlign = Paint.Align.LEFT
        val ascent = -paint.ascent() // ascent() is negative
        val descent = paint.descent()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val width = bounds.width()+1
        val height = (ascent + descent).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text, 0f, ascent, paint)
        return image
    }


    private fun onFxAlienExplodes(pos: PointF, sprite: Sprite?, textureId: Int) {
        renderSpriteIntoParticles(sprite, textureId, pos, 1f, 10f)
        soundPlayer.play(SoundType.InvaderExplode)
    }


    private fun onFxShelterDamaged(pos: PointF, severity: Float) {
        val count = 30

        for (i in 0 until count) {
            val alpha = Random.nextFloat() * 360f
            val lifetime = .6f + Random.nextFloat() * .5f
            val speed = (15f + Random.nextFloat() * 10f) * severity
            val r = 1f
            particleEngine.add(
                SimpleParticle(
                    pos.x,
                    pos.y,
                    r,
                    alpha,
                    speed,
                    lifetime,
                    Color.rgb(0, 192, 0)
                )
            )
        }
        soundPlayer.play(SoundType.DamageShelter)
    }

    private fun onFxPlayerShipExplodes(
        pos: PointF,
        severity: Float,
        sprite: Sprite?,
        textureId: Int
    ) {
        val count = 200

        for (i in 0 until count) {
            val alpha = Random.nextFloat() * 360f
            val lifetime = 3.6f + Random.nextFloat() * .5f
            val speed = (15f + Random.nextFloat() * 20f) * severity
            val r = .5f + Random.nextFloat() * .4f
            particleEngine.add(
                SimpleParticle(
                    pos.x,
                    pos.y,
                    r,
                    alpha,
                    speed,
                    lifetime,
                    Color.WHITE
                )
            )
        }
        renderSpriteIntoParticles(sprite, textureId, pos, 3f, 15f)
        soundPlayer.play(SoundType.PlayerExplode)
    }

    private fun onFxAlienLanded(pos: PointF, severity: Float) {
        val count = 3

        for (i in 0 until count) {
            val alpha = Random.nextFloat() * 360f
            val lifetime = .6f + Random.nextFloat() * .5f
            val speed = (5f + Random.nextFloat() * 10f) * severity
            val r = 1f
            particleEngine.add(SimpleParticle(pos.x, pos.y, r, alpha, speed, lifetime, Color.WHITE))
        }
        soundPlayer.play(SoundType.Defeated)
    }

    private fun onFxWaveCompleted(pos: PointF, severity: Float) {
    }

    private fun onFxGameOver(pos: PointF, severity: Float) {
        soundPlayer.play(SoundType.GameOver)
    }

    private fun onFxAlienShooting(pos: PointF, severity: Float) {
        for (n in 0..3) {
            val alpha0 = n * 90f
            val alpha1 = alpha0 + 45f
            particleEngine.add(
                GrowingParticle(
                    pos.x,
                    pos.y,
                    .1f,
                    4f,
                    alpha0,
                    alpha1,
                    VScreen.width * .1f,
                    .5f,
                    Color.WHITE,
                    0f
                )
            )
        }
        soundPlayer.play(SoundType.InvaderShooting)
    }

    private fun onFxAsteroidExplodes(origin:PointF, size: Float){
        soundPlayer.play(SoundType.InvaderExplode)
        val count = 20

        for (i in 0 until count) {
            val alpha = Random.nextFloat() * 360f
            val lifetime = .5f + Random.nextFloat()*.5f
            val speed = (15f + Random.nextFloat() * 20f)
            val r = .5f + Random.nextFloat() * .4f
            particleEngine.add(
                SimpleParticle(
                    origin.x,
                    origin.y,
                    r,
                    alpha,
                    speed,
                    lifetime,
                    Color.WHITE
                )
            )
        }
    }

    fun renderBigCenterText(text: String, paint: Paint) {
        if (text.isEmpty())
            return

        val radiusScale=.1f
        val delaySec=1f
        val bmp = textAsBitmap(text, paint)
        val center=PointF(VScreen.centerX, VScreen.centerY)
        bitmapDisassembler(bmp, center, radiusScale, PairF(radiusScale, radiusScale*2f), PairF(1f, 5f), delaySec,1f,false)
    }

    fun renderTinyScoreText(text: String, paint: Paint, center:PointF) {
        if (text.isEmpty())
            return

        val radiusScale=.1f
        val bmp = textAsBitmap(text, paint)
        bitmapDisassembler(bmp, center, radiusScale, PairF(radiusScale, .2f), PairF(.05f, .5f), .5f,.1f, true)
    }


}

