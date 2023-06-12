package eu.imeon.invaders.core.collision

import android.graphics.Point
import android.graphics.RectF

interface Hitable {
    fun getHitRect(): RectF
    fun onHit(source: Hitable):Boolean
    fun performerType(): PerformerType
    fun isWhere(): Point = Point(0,0)
    fun hasSuperpower(): Boolean = false

}
