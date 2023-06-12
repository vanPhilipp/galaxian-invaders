package eu.imeon.invaders.core.collision

import android.graphics.RectF

class ColliderTreeNode(val rect: RectF, firstCitizen: Hitable? = null) {
    val childs = arrayListOf<ColliderTreeNode?>(null, null, null, null)
    val hitables = arrayListOf<Hitable>()
    val quarter = arrayListOf<RectF>()
    private var freshId = -1

    init {
        val w2 = rect.width() / 2
        val h2 = rect.height() / 2
        for (x in 0..1) {
            for (y in 0..1) {
                quarter.add(
                    RectF(
                        rect.left + w2 * x,
                        rect.top + h2 * y,
                        rect.left + w2 * (x + 1),
                        rect.top + h2 * (y + 1)
                    )
                )
            }
        }
        if (firstCitizen != null) {
            hitables.add(firstCitizen)
            // In case new stuff comes in, move to this child quarter
            freshId = checkQuarters(firstCitizen.getHitRect())
        }
    }

    fun checkQuarters(src: RectF): Int {
        for (i in 0 until quarter.size) {
            if (quarter[i].contains(src))
                return i
        }
        return -1
    }

    fun unfresh() {
        if (freshId < 0)
            return

        if (hitables.size != 1) {
            throw ColliderException("At this point size should be 1!!!!")
        }
        for (child in childs) {
            if (child != null) {
                throw ColliderException("A fresh node must have no childs!!!!")
            }
        }
        if (freshId > quarter.size) {
            throw ColliderException("The freshId is out of range: $freshId!!!!")
        }
        childs[freshId] = ColliderTreeNode(quarter[freshId], hitables[0])
        hitables.clear()
        freshId = -1
    }

}
