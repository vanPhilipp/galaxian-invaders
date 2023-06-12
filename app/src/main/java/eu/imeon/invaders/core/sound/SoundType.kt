package eu.imeon.invaders.core.sound

enum class SoundType(
    val filename: String,
    val lockTime: Float = 0f,
    val volume: Float = 1f,
    val loop: Int = 0
) {
    Shoot("shoot.ogg", .3f, .5f), // onJoystickFire
    InvaderExplode("invaderexplode.ogg", .1f, 1f), // onFxAlienExplode
    DamageShelter("damageshelter.ogg", .1f), // onFxShelterDamaged
    PlayerExplode("playerexplode.ogg"), // onFxPlayerShipExplode
    Invader1("invader1.ogg"),
    Invader2("invader2.ogg"),
    Invader3("invader3.ogg"),
    Invader4("invader4.ogg"),
    Defeated("defeated.ogg"), // onFxAlienLanded
    GameOver("game-over.ogg"),
    MysteryUFO("mystery-ufo.ogg", 0f, 1f, -1),

    // FIXME: Volume settings seem to not working properly.
    InvaderShooting("invader-shoot.ogg", 0f, 1f, 0)
}
