package eu.imeon.invaders.core.gamestate

import android.util.Log
import eu.imeon.invaders.core.gameobjects.menu.GameVariant

class GlobalGameState {

    companion object {

        var waves = 0
        var difficultyLevel=0
        var gameVariant=GameVariant.RoundTrip

        var runtime = 0f
            private set

        var score=0
        var lives=0
        var godMode=false
        var peaceOnEarth=false
        var lazyAliens=false

        private var automaticGoto = GameState.None
        private var automaticAt = 0f
        var currentState = GameState.WaveInProgress
            private set

        fun setGameState(gameState:GameState){
            if (currentState == gameState) {
                Log.w(this::class.simpleName, "GameState was already set: $gameState")
            }

            currentState=gameState
            runtime = 0f
            automaticGoto = gameState.autoNextGameState ?: GameState.None
            automaticAt = gameState.delaySec
        }

        fun message(languageId:Int):String{
            val max=currentState.message.size-1
            return currentState.message[languageId.coerceIn(0,max)]
        }

        fun updateGameClocks(deltaT: Float):Boolean {
            runtime += deltaT

            if (automaticGoto == GameState.None || runtime < automaticAt)
                return false

            Log.i(this::class.simpleName, "Switching to game state $automaticGoto")
            setGameState(automaticGoto)
            return true
        }

    }


}