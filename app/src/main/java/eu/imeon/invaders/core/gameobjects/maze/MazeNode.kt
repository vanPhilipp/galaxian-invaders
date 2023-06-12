package eu.imeon.invaders.core.gameobjects.maze

class MazeNode(val mazeItem:MazeItem) {
    var pillState=mazeItem.pillType!=PillType.NONE
    var x:Int=0
    var y:Int=0
}
