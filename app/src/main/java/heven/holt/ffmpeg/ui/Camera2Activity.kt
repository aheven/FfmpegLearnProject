package heven.holt.ffmpeg.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import heven.holt.ffmpeg.R
import kotlinx.android.synthetic.main.activity_camera.*
import java.nio.ByteBuffer
import kotlin.math.abs


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Activity : AppCompatActivity(), SurfaceHolder.Callback {
    companion object {
        fun startAtc() {
            ActivityUtils.startActivity(Camera2Activity::class.java)
        }
    }

    private var mPreviewSize: Size? = null
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        surface_view.holder.addCallback(this)
    }

    private fun openCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cameraId = cameraIdList.first()
        mPreviewSize = getMatchingSize()
        surface_view.resize(mPreviewSize?.width?:-1,mPreviewSize?.height?:-1)
        cameraManager.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onOpened(camera: CameraDevice) {
                val surfacePreview = Surface(surface_view.surfaceControl)
                val imageReader = ImageReader.newInstance(mPreviewSize?.width?:0,mPreviewSize?.height?:0,ImageFormat.YUV_420_888,2)
                imageReader.setOnImageAvailableListener({
                    val image: Image = it.acquireLatestImage() //最后一帧

                    //do something
                    //do something
                    val len: Int = image.planes.size
                    val bytes = arrayOfNulls<ByteArray>(len)
                    var count = 0
                    for (i in 0 until len) {
                        val buffer: ByteBuffer = image.planes[i].buffer
                        val remaining: Int = buffer.remaining()
                        val data = ByteArray(remaining)
                        val _data = ByteArray(remaining)
                        buffer.get(data)
                        System.arraycopy(data, 0, _data, 0, remaining)
                        bytes[i] = _data
                        count += remaining
                    }
                    //数据流都在 bytes[][] 中，关于有几个plane，可以看查看 ImageUtils.getNumPlanesForFormat(int format);
                    // ...
                    //数据流都在 bytes[][] 中，关于有几个plane，可以看查看 ImageUtils.getNumPlanesForFormat(int format);
                    // ...
                    LogUtils.e("dataSize=${count}")
                    image.close() //一定要关闭

                },null)
                camera.createCaptureSession(
                    listOf(surfacePreview,imageReader.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            val createCaptureRequest =
                                camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                            //设置自动对焦
                            createCaptureRequest.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )

                            createCaptureRequest.addTarget(surfacePreview)
                            createCaptureRequest.addTarget(imageReader.surface)
                            session.setRepeatingRequest(createCaptureRequest.build(), null, null)
                        }
                    },
                    null
                )
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }
        }, null)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mPreviewSize = Size(surface_view.width, surface_view.height)
        openCamera()
    }

    /**
     * 获取匹配的大小 这里是Camera2获取分辨率数组的方式,Camera1获取不同,计算一样
     * @return
     */
    private fun getMatchingSize(): Size? {
        var selectSize: Size? = null
        var selectProportion = 0f
        try {
            val viewProportion =
                surface_view.width.toFloat() / surface_view.height.toFloat() //计算View的宽高比
            val cameraCharacteristics: CameraCharacteristics =
                cameraManager.getCameraCharacteristics(cameraId!!)
            val streamConfigurationMap =
                cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes =
                streamConfigurationMap!!.getOutputSizes(ImageFormat.JPEG)
            for (i in sizes.indices) {
                val itemSize = sizes[i]
                val itemSizeProportion =
                    itemSize.height.toFloat() / itemSize.width.toFloat() //计算当前分辨率的高宽比
                val differenceProportion =
                    abs(viewProportion - itemSizeProportion) //求绝对值
                Log.e(javaClass.simpleName, "相减差值比例=$differenceProportion")
                if (i == 0) {
                    selectSize = itemSize
                    selectProportion = differenceProportion
                    continue
                }
                if (differenceProportion <= selectProportion) { //判断差值是不是比之前的选择的差值更小
                    if (differenceProportion == selectProportion) { //如果差值与之前选择的差值一样
                        if (selectSize!!.width + selectSize.height < itemSize.width + itemSize.height) { //选择分辨率更大的Size
                            selectSize = itemSize
                            selectProportion = differenceProportion
                        }
                    } else {
                        selectSize = itemSize
                        selectProportion = differenceProportion
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.e(javaClass.simpleName, "getMatchingSize: 选择的比例是=$selectProportion")
        Log.e(
            javaClass.simpleName,
            "getMatchingSize: 选择的尺寸是 宽度=" + selectSize!!.width + "高度=" + selectSize.height
        )
        return selectSize
    }
}
