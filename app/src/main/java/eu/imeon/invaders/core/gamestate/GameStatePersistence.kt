package eu.imeon.invaders.core.gamestate

import android.content.SharedPreferences

class PersistentInt(private val prefs: SharedPreferences, val name: String, defaultValue: Int) {
    var value = prefs.getInt(name, defaultValue)

    fun save() {
        val oldValue = prefs.getInt(name, 0)
        if (value != oldValue) {
            val editor = prefs.edit()
            editor.putInt(name, value)
            editor.apply()
        }
    }
}

class GameStatePersistence(sharedPreferences: SharedPreferences) {

    var highScore = PersistentInt(sharedPreferences, "highscore", 0)

    fun save() {
        highScore.save()
    }

}