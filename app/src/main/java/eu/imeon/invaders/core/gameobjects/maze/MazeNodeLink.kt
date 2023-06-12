package eu.imeon.invaders.core.gameobjects.maze

enum class MazeNodeLink(val bitMask:Int) {
    NONE(0),
    UP(1),
    DOWN(2),
    LEFT(4),
    RIGHT(8)
}