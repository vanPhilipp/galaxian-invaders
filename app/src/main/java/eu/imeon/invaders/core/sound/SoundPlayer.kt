package eu.imeon.invaders.core.sound

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

class SoundPlayer(context: Context) {

    private val soundPool: SoundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)

    private val singleSoundPlayers = mutableMapOf<SoundType, SingleSoundPlayer>()

    init {
        val assetManager = context.assets
        SoundType.values().onEach {
            singleSoundPlayers[it] = SingleSoundPlayer(soundPool, it, assetManager)
        }
    }

    fun play(soundType: SoundType): Boolean {
        return singleSoundPlayers[soundType]?.play() ?: false
    }

    fun stop(soundType: SoundType) {
        singleSoundPlayers[soundType]?.stop()
    }

    fun shutdown() {
        singleSoundPlayers.values.onEach { it.stop() }
    }
}