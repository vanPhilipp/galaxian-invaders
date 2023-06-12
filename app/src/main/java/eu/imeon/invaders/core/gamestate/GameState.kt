package eu.imeon.invaders.core.gamestate

enum class GameState(
    val message: List<String>,
    val autoNextGameState: GameState?,
    val delaySec: Float = 0f
) {
    None(
        listOf("Idle"),
        None
    ),
    WaveInProgress(
        listOf("", ""),
        None
    ),
    GameOver(
        listOf("Game Over", "Spielende"),
        None
    ),
    AlienLanded(
        listOf("Invaders won!", "Verloren!"),
        GameOver, 3f
    ),
    RespawnPhase(
        listOf("", ""),
        None
    ),
    PlayerShipExplodes(
        listOf("Ship lost", "Schiff verloren!"),
        RespawnPhase, 3f
    ),
    PlayerDefeated(
        listOf("Last ship lost!", "Verloren!"),
        GameOver, 2f
    ),
    WaveStarting(
        listOf("GO!", "Los geht's!"),
        WaveInProgress, 1f
    ),
    GameStarting(
        listOf("New Game", "Neues Spiel!"),
        WaveStarting, .5f
    ),
    WaveCompleted(
        listOf("Wave completed", "Runde gewonnen!"),
        WaveStarting, 4f
    )
}