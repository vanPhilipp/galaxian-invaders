package eu.imeon.invaders.core.tests.spritetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import eu.imeon.invaders.core.util.Sprite

class SpriteUnit(val sprite: Sprite, val pos: PointF) {

    var active=true

    fun draw(canvas: Canvas, paint: Paint){
        if (active)
            sprite.draw(canvas, paint, pos)
    }
}
