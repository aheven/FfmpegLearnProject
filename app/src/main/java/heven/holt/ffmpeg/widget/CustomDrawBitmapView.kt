package heven.holt.ffmpeg.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.ImageUtils
import heven.holt.ffmpeg.R

/**
 *Time:2020/4/7
 *Author:HevenHolt
 *Description:
 */
class CustomDrawBitmapView(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs) {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    constructor(context: Context) : this(context, null)

    private val bitmap: Bitmap = ImageUtils.getBitmap(R.mipmap.ic_launcher)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
    }
}