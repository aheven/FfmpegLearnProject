### Android音视频开发（四）：使用Camera Api采集视频数据

本文只要将使用Camera Api采集视频数据并保存到文件，分别使用SurfaceView、TextureView来预览Camera数据，获取到NV21的数据回调。

**一、SurfaceView预览Camera数据**

Android开发有两种方式渲染视频，SurfaceView与TextureView。

下面是使用SurfaceView预览数据的方式：

1. 创建Camera对象

```kotlin
camera = Camera.open()
camera.setDisplayOrientation(90)
val parameters = camera.parameters
//设置回调的格式为NV21数据
parameters.previewFormat = ImageFormat.NV21
camera.parameters = parameters
camera.setPreviewCallback(this)
```

2. 设置Surface的监听接口

```kotlin
texture_view.surfaceTextureListener = this
```

```kotlin
override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

}

override fun surfaceDestroyed(holder: SurfaceHolder?) {
    camera.stopPreview()
    camera.setPreviewCallback(null)
    camera.release()
}

override fun surfaceCreated(holder: SurfaceHolder?) {
    try {
        //设置使用SurfaceView预览数据
        camera.setPreviewDisplay(holder)
        camera.startPreview()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
```

3. 在`onPreviewFrame(ByteArray,Camera)`中过去NV21格式的ByteArray数据：

```kotlin
override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
    LogUtils.e("dataSize=${data?.size}")
}
```

**二、TextureView预览Camera数据**

1. 创建Camera对象，同上
2. 设置TextureView监听接口：

```kotlin
 texture_view.surfaceTextureListener = this
```

```kotlin
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
```

> 本章设计类：`CameraActivity`

