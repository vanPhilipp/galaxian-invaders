package eu.imeon.invaders.core.util

import android.graphics.*
import android.util.Log

// Given must be the width, related to the virtual screen.

class BackdropScaler(private val bitmap: Bitmap) {

    private var scaledBitmap = bitmap
    private val bmpRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

    fun draw(canvas: Canvas, paint: Paint) {

        val w1 = canvas.width
        val h1 = canvas.height
        if (w1 != scaledBitmap.width || h1 != scaledBitmap.height) {
            Log.i("debug","BitmapScaler rescales to $w1 x $h1")
            val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
            scaledBitmap = if (bmpRatio > canvasRatio) {
                val width1 = canvas.height.toFloat() * bmpRatio
                val bmp1 = Bitmap.createScaledBitmap(bitmap, width1.toInt(), canvas.height, true)
                val left = (width1 - canvas.width) / 2
                Bitmap.createBitmap(bmp1, left.toInt(), 0, canvas.width, canvas.height)
            } else {
                Bitmap.createScaledBitmap(bitmap, canvas.width, canvas.height, true)
            }

        }
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
    }
}
