package eu.imeon.invaders.core.util

import android.content.res.Resources
import android.graphics.*
import android.util.Log
import android.util.Size
import android.util.SizeF
import eu.imeon.invaders.core.vscreen.VScreen2CanvasTransformer

// Given must be the width, related to the virtual screen.

class Sprite(resources:Resources, id:Int, private val vWidth: Float, private val grid:Size) {

    private val options=BitmapFactory.Options().apply { inScaled=false }
    private var bitmap=BitmapFactory.decodeResource(resources, id, options)
    private var scaledBitmaps = arrayListOf<Bitmap>()
    private val ratio=(bitmap.width/grid.width).toFloat() / (bitmap.height/grid.height).toFloat()
    private val vHeight=vWidth/ratio
    val size = SizeF(vWidth, vHeight)
    private var scaledSize=Size(0,0)

    private fun createScaledBitmaps(w1:Int, h1:Int){
        Log.i("Sprite","createScaledBitmaps ($w1 x $h1)")
        val w0=bitmap.width/grid.width
        val h0=bitmap.height/grid.height
        scaledBitmaps = ArrayList<Bitmap>()
        for (i in 0 until grid.width) {
            for (k in 0 until grid.height) {
                val x0=w0 * i
                val y0=h0 * k
                val bmp0= Bitmap.createBitmap(bitmap, x0, y0,w0,h0 )
                val bmp1 = Bitmap.createScaledBitmap(bmp0, w1, h1, false)
                scaledBitmaps.add(bmp1)
            }
        }
        scaledSize=Size(w1,h1)
    }

    fun getScaledBitmap(id:Int):Bitmap?{
        if (id<0 || id>= scaledBitmaps.size)
            return null
        return scaledBitmaps[id]
    }

    private fun updateScaledBitmaps(transformer: VScreen2CanvasTransformer){
        val sizeScreen = transformer.toCanvas(size)
        val w1 = sizeScreen.width.toInt().coerceAtLeast(1)
        val h1 = sizeScreen.height.toInt().coerceAtLeast(1)
        if (w1 != scaledSize.width || h1 != scaledSize.height) {
            createScaledBitmaps(w1,h1)
        }
    }
    fun draw(canvas: Canvas, paint: Paint, pos: PointF, id:Int=0) {
        val transformer = VScreen2CanvasTransformer(canvas)
        updateScaledBitmaps(transformer)
        val screenPos = transformer.toCanvas(pos)
        val bmp:Bitmap=getScaledBitmap(id) ?: return
        canvas.drawBitmap(bmp, screenPos.x, screenPos.y, paint)
    }
    fun drawRotated(canvas: Canvas, paint: Paint, pos: PointF, alpha:Float, id:Int=0) {
        val transformer = VScreen2CanvasTransformer(canvas)
        updateScaledBitmaps(transformer)
        val screenPos = transformer.toCanvas(pos)
        val bmp:Bitmap=getScaledBitmap(id) ?: return
        val rotator = Matrix()
        val degreeAlpha=Math.toDegrees(alpha.toDouble()).toFloat()
        val hx=scaledSize.width/2
        val hy=scaledSize.height/2
        rotator.postTranslate(-hx.toFloat(), -hy.toFloat())
        rotator.postRotate(degreeAlpha)
        rotator.postTranslate(screenPos.x, screenPos.y)
        //canvas.drawBitmap(bmp, screenPos.x, screenPos.y, paint)
        canvas.drawBitmap(bmp, rotator, paint)
    }

    fun vPixelSize():Float{
        return if (scaledSize.width>0) vWidth/ scaledSize.width.toFloat() else .0001f
    }
}
