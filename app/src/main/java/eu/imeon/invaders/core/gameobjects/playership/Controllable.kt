package eu.imeon.invaders.core.gameobjects.playership

interface Controllable {

    fun onControllerRestart(){}
    fun onControllerFire(){}
    fun onControllerThrust(set:Boolean) {}
    fun onControllerPan(dir: Int){}
    fun onControllerTilt(dir: Int){}
    fun onControllerKill(){}

}