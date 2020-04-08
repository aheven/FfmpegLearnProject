package heven.holt.ffmpeg.ui

import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ImageUtils
import heven.holt.ffmpeg.R
import kotlinx.android.synthetic.main.activity_draw_bitmap.*

class DrawBitmapActivity : AppCompatActivity() {
    companion object {
        fun startAtc() {
            ActivityUtils.startActivity(DrawBitmapActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_bitmap)

        initImageView()
        initSurfaceView()
    }

    private fun initImageView() {
        image_view.setImageBitmap(ImageUtils.getBitmap(R.mipmap.ic_launcher))
    }

    private fun initSurfaceView() {
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
                val bitmap = ImageUtils.getBitmap(R.mipmap.ic_launcher)

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
    }
}
