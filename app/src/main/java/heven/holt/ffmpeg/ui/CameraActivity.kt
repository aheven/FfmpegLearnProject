package heven.holt.ffmpeg.ui

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import heven.holt.ffmpeg.R
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.IOException

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PreviewCallback,TextureView.SurfaceTextureListener {
    companion object {
        fun startAtc() {
            ActivityUtils.startActivity(CameraActivity::class.java)
        }
    }

    private lateinit var camera: Camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

//        initSurfaceView()
        initTextureView()
        camera = Camera.open()
        camera.setDisplayOrientation(90)
        val parameters = camera.parameters
        parameters.previewFormat = ImageFormat.NV21
        camera.parameters = parameters
        camera.setPreviewCallback(this)
    }

    private fun initTextureView() {
//        texture_view.surfaceTextureListener = this
    }

    private fun initSurfaceView() {
//        surface_view.holder.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        camera.stopPreview()
        camera.setPreviewCallback(null)
        camera.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        LogUtils.e("dataSize=${data?.size}")
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        camera.stopPreview()
        camera.setPreviewCallback(null)
        camera.release()
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        try {
            camera.setPreviewTexture(surface)
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
