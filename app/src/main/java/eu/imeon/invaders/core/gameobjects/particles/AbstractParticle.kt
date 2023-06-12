package eu.imeon.invaders.core.gameobjects.particles

import android.graphics.Canvas
import android.graphics.Paint

abstract class AbstractParticle {

    abstract fun draw(canvas: Canvas, paint: Paint)

    abstract fun update(deltaT: Float): Boolean

}