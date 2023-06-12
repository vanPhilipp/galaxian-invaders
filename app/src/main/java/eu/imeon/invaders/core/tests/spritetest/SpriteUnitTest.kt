package eu.imeon.invaders.core.tests.spritetest

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.Theme
import eu.imeon.invaders.core.collision.SuperCollider
import eu.imeon.invaders.core.effects.GameEvent
import eu.imeon.invaders.core.effects.GameEventLambda
import eu.imeon.invaders.core.vscreen.VScreen



class SpriteUnitTest(
    theme:Theme,
    val gameEventLambda: GameEventLambda,
) : GameObject {

    private val paint=Paint()

    private val sprites = theme.invaders

    private var spriteUnits=ArrayList<SpriteUnit>()


    override fun prepareLevel(difficultyLevel: Int) {
        spriteUnits=ArrayList<SpriteUnit>()
        for (x in sprites.indices){
            val range=VScreen.width*.5f
            val margin=VScreen.width*.25f
            val normalized=x.toFloat() / (sprites.size-1).toFloat()
            val pos=PointF(margin + range*normalized, VScreen.height/2f)
            spriteUnits.add(SpriteUnit(sprites[x], pos))
        }

    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.argb(255, 0, 255, 0)

        spriteUnits.forEach { it.draw(canvas, paint) }
    }




    override fun update(deltaT: Float) {
    }


    override fun prepareGame() {
    }

    override fun registerHitables(superCollider: SuperCollider) {
    }

    override fun checkCollisions(superCollider: SuperCollider) {
    }

    fun autoKillNext(){
        val target=spriteUnits.firstOrNull { it.active } ?: return
        target.active=false
        gameEventLambda(GameEvent.AlienExplodes, target.pos, 1f, target.sprite,0)

    }
}