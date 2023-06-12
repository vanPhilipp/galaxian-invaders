package eu.imeon.invaders.core.gameobjects.maze

enum class Directions(val dx: Int, val dy: Int, val index: Int) {
    UP(0, -1, 0),
    RIGHT(1, 0, 1),
    DOWN(0, 1, 2),
    LEFT(-1, 0, 3),
}