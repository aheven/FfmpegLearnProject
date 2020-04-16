### Android音视频开发（五）：使用Camera2 Api采集视频数据

​	从Android5.0开始，Google引入了一套全新的相机框架，并且废弃了原有的Camera框架。Camera2框架获取预览视频数据的流程：

1. 获取CameraManager，CameraManager是一个负责查询和建立相机的系统服务，其主要功能有：

- 将相机信息封装到CameraCharacteristics中，并提取获得CameraCharacteristics的实例方式
- 根据指定的相机ID连接相机设备
- 提供将闪光灯设置成手电筒的快捷方式

```kotlin
cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
```

2. 通过`cameraManager.getCameraCharacteristics(String cameraId)`获取摄像头适配的相机信息，获取到合适的宽高比：

```kotlin
/**
 * 获取匹配的大小 这里是Camera2获取分辨率数组的方式,Camera1获取不同,计算一样
 * @return
 */
private fun getMatchingSize(): Size? {
    var selectSize: Size? = null
    var selectProportion = 0f
    try {
        val viewProportion =
            texture_view.width.toFloat() / texture_view.height.toFloat() //计算View的宽高比
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
```

3. 通过`cameraManager.openCamera(String cameraId,CameraDevice.StateCallback callback,Handler handler)`打开摄像头并获取CameraDevice：

```kotlin
cameraManager.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
    override fun onOpened(camera: CameraDevice) {
        //get CameraDevice
    }
    
    override fun onDisconnected(camera: CameraDevice) {
    }
    
    override fun onError(camera: CameraDevice, error: Int) {
    }
}
```

4. 获取到CameraDevice后，调用`cameraDevice.createCaptureSession(List<Surface> outputs,CameraCaptureSession.StateCallback callback,Handler handler)`创建CameraCaptureSession；

   CameraCaptureSession是一个事务，用来向相机设备发送获取图像的请求。

```kotlin
camera.createCaptureSession(
    //拍照的session，这里有两个surface，一个是手机的界面，一个是手机的图片
    //也就是说，这个session形成的数据流，可以一个传向手机界面，一个形成图片
    listOf(surfacePreview, imageReader.surface),
    object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
        }

        override fun onConfigured(session: CameraCaptureSession) {
            //不管是预览还是拍照，程序都调用camera.createCaptureRequest(int templateType)创建CaptureRequest.Builder。
            //该方法支持TEMPLATE_PREVIEW（预览）、TEMPLATE_RECORD（拍摄视频）、TEMPLATE_STILL_CAPTURE（拍照）等参数。
            val createCaptureRequest =
                camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            //设置自动对焦
            createCaptureRequest.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )

            createCaptureRequest.addTarget(surfacePreview)
            createCaptureRequest.addTarget(imageReader.surface)
            //请求重复捕捉画面，常用于预览或连拍场景
            session.setRepeatingRequest(createCaptureRequest.build(), null, null)
        }
    },
    null
)
```

5. 获取Camera原始数据，相对与camera来说，camera2并没有提供`setPreviewCallbakc(ByteArray array)`函数，在Camera2 api中，Camera的原始数据需要通过ImageReader获得：

```kotlin
val surfacePreview = Surface(texture_view.surfaceTexture)
val imageReader = ImageReader.newInstance(
    mPreviewSize?.width ?: 0,
    mPreviewSize?.height ?: 0,
    ImageFormat.YUV_420_888,
    2
)
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

}, null)
```

以上为获取Camera2原始数据的整体流程，以后还需要深入了解camera2 api的使用方式。

> 本章涉及到的类有：`Camera2Activity`