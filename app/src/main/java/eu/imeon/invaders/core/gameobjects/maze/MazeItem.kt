package eu.imeon.invaders.core.gameobjects.maze

enum class MazeItem(val isWall:Boolean=true, val pillType:PillType=PillType.NONE) {
    NONE(false),
    PILL(false, PillType.PILL),
    POWER_PILL(false, PillType.POWER_PILL),
    HORIZONTAL_WALL,
    VERTICAL_WALL,
    GHOST_DOOR(false),
    PACMAN_HOME(false),
    ARC1,
    ARC2,
    ARC3,
    ARC4,
}