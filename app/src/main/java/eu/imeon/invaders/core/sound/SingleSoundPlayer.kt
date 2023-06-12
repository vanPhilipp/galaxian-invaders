package eu.imeon.invaders.core.sound

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.SoundPool
import android.util.Log
import java.io.IOException

class SingleSoundPlayer(
    private val soundPool: SoundPool,
    private val soundType: SoundType,
    assetManager: AssetManager,
) {

    private lateinit var descriptor: AssetFileDescriptor
    private var id: Int = 0
    private var timeStampStart = 0L
    private var streamID: Int = 0

    init {
        try {
            descriptor = assetManager.openFd(soundType.filename)
            id = soundPool.load(descriptor, 0)
        } catch (e: IOException) {
            Log.e("error", "failed to load sound file ${soundType.toString()} '${soundType.filename}'")
        }
    }

    fun play(): Boolean {
        val now = System.currentTimeMillis()
        if (timeStampStart > 0) {
            val playTime = (now - timeStampStart) / 1000f
            if (playTime < soundType.lockTime) {
//                val text = String.format(
//                    "Sound playback ignored, previous ${soundType.name} playing only since %.3f sec, lockTime=%.3f sec",
//                    playTime, soundType.lockTime
//                )
//                println(text)
                return false
            }
        }

        streamID = soundPool.play(id, soundType.volume, soundType.volume, 0, soundType.loop, 1f)
        timeStampStart = now
        return true
    }

    fun stop() {
        if (streamID != 0)
            soundPool.stop(streamID)
    }
}
