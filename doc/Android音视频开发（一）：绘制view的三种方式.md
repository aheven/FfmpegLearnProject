### Android音视频开发（一）：绘制view的三种方式

想要逐步入门音视频开发，就需要一步步学习整理和积累相关的音视频开发知识。本文是音视频学习的第一篇，在Android平台下绘制一张图片，分别使用`ImageView`、`SurfaceView`、`自定义View`的方式。

**1.	ImageView绘制图片**

在Android平台中绘制图片的最基本方式，获取图片的bitmap对象，调用`ImageView`的`setImageBitmap(Bitmap bitmap)`函数设置图片。

首先，我们先从资源文件中获取App的启动图片`ic_launcher`，本章所有获取图片均用此方式获取：

```kotlin
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
```

在界面中绘制图片

```kotlin
image_view.setImageBitmap(ImageUtils.getBitmap(this, R.mipmap.ic_launcher))
```

**2.	SurfaceView绘制图片**

相对于ImageView来说，SurfaceView图片绘制要复杂一些：

```kotlin
//设置surfaceView透明
surface_view.setZOrderOnTop(true)
surface_view.holder.setFormat(PixelFormat.TRANSPARENT)
surface_view.holder.addCallback(object : SurfaceHolder.Callback {
    override fun surfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int
    ) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (holder == null) return

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val bitmap =
            ImageUtils.getBitmap(this@DrawBitmapActivity, R.mipmap.ic_launcher) ?: return

        //设置surfaceView控件的大小
        val layoutParams = surface_view.layoutParams
        layoutParams.width = bitmap.width
        layoutParams.height = bitmap.height
        surface_view.layoutParams = layoutParams

        val canvas = holder.lockCanvas()//先锁定当前surfaceView的画布
        canvas.drawBitmap(bitmap, 0f, 0f, paint)//执行绘制操作
        holder.unlockCanvasAndPost(canvas)//解除锁定并显示在界面上
    }

})
```

**3.	自定义View绘制图片**

类似于SurfaceView创建图片，自定义view中也可以通过Paint在Canvas中绘制图片：

```kotlin
class CustomDrawBitmapView(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs) {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    constructor(context: Context) : this(context, null)

    private val bitmap: Bitmap? = ImageUtils.getBitmap(context, R.mipmap.ic_launcher)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitmap?.let { canvas?.drawBitmap(it, 0f, 0f, paint) }
    }
}
```



[^本章涉及类]: `DrawBitmapActivity``CustomDrawBitmapView``ImageUtils`

