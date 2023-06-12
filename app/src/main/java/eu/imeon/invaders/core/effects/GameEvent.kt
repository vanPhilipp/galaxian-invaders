package eu.imeon.invaders.core.effects

import eu.imeon.invaders.core.gamestate.GameState

enum class GameEvent(val score:Int=0,val renderScore:Boolean=false, val gameState: GameState = GameState.None) {
    AlienExplodes(100,true),
    AsteroidExplodes(100,true),
    MysteryShipExplodes(500, true),
    ShelterDamaged(10),
    AlienShooting,
    PlayerShipExplodes(gameState=GameState.PlayerShipExplodes),
    PlayerDefeated(gameState=GameState.PlayerDefeated),
    AlienLanded(gameState=GameState.AlienLanded),
    WaveCompleted(gameState=GameState.WaveCompleted),
    GameOver(gameState=GameState.GameOver),
    StartNewGame(gameState=GameState.WaveStarting)
}