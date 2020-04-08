package heven.holt.ffmpeg.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 *Time:2020/4/8
 *Author:HevenHolt
 *Description:
 */
object ImageUtils {
    fun getBitmap(context: Context, @DrawableRes redId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, redId) ?: return null
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }
}