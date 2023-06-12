package eu.imeon.invaders.core.collision

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.component3
import androidx.core.graphics.component4
import eu.imeon.invaders.core.GameObject
import eu.imeon.invaders.core.vscreen.VScreen
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer

typealias ColliderTestCallback = (Hitable) -> Unit

class SuperCollider : GameObject {

    private var root = ColliderTreeNode(VScreen.rect)
    private val maxDepth = 5
    private val paintQuarter = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.MAGENTA
    }
    private val paintHitable = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.CYAN
    }
    private var count = 0

    fun register(hitable: Hitable) {
        count++
        registerRecursive(hitable, 0, root)
    }

    private fun registerRecursive(hitable: Hitable, depth: Int = 0, node: ColliderTreeNode = root) {
        if (depth < maxDepth) {
            val quarterId = node.checkQuarters(hitable.getHitRect())
            if (quarterId >= 0) {
                val child = node.childs[quarterId]
                if (child == null) {
                    node.childs[quarterId] = ColliderTreeNode(node.quarter[quarterId], hitable)
                } else {
                    child.unfresh()
                    registerRecursive(hitable, depth + 1, child)
                }
                return
            }
        }
        node.hitables.add(hitable)
    }

    private fun testRecursive(
        attacker: RectF,
        node: ColliderTreeNode,
        callback: ColliderTestCallback
    ) {
        node.hitables.onEach {
            val (left, top, right, bottom) = it.getHitRect()
            if (attacker.intersects(left, top, right, bottom)) {
                callback(it)
            }
        }
        node.quarter.onEachIndexed { i, (left, top, right, bottom) ->
            if (node.childs[i] != null && attacker.intersects(left, top, right, bottom)) {
                testRecursive(attacker, node.childs[i]!!, callback)
            }
        }
    }

    fun test(attacker: RectF, callback: ColliderTestCallback) {
        return testRecursive(attacker, root, callback)
    }

    private fun drawRecursive(canvas: Canvas, node: ColliderTreeNode) {
        val transformer = VScreen2CanvasTransformer(canvas)

        if (node.hitables.size > 0) {
            transformer.toCanvas(node.rect).let {
                canvas.drawRect(it, paintQuarter)
            }
        }
        node.hitables.onEach { hitable ->
            transformer.toCanvas(hitable.getHitRect()).let {
                canvas.drawRect(it, paintHitable)
            }
        }
        node.childs.filterNotNull().onEach {
            drawRecursive(canvas, it)
        }
    }

    override fun drawDebug(canvas: Canvas) {
        drawRecursive(canvas, root)
    }

    override fun update(deltaT: Float) {
        count = 0
        root = ColliderTreeNode(VScreen.rect)
    }

    override fun prepareLevel(difficultyLevel: Int) {
    }

    override fun prepareGame() {
    }
}
