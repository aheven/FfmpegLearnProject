### Android音视频开发（三）：使用Audio Track播放PCM音频

AudioTrack类负责Android平台上的音频数据输出任务。默认有两种输出模式（MODE_STREAM与MODE_STATIC），分别加载音频流模式与数据加载模式，对应着两个完全不同的使用场景。

- MODE_STREAM：在这种模式下，通过`write`函数分次将音频数据写入到AudioTrack中。这种方式和文件写入方式类似，但是这种工作方式需要把数据从用户提供的buffer中拷贝到AudioTrack内部的buffer中，这在一定程度上会有延时。为了解决这个问题，AudioTrack引入了第二种模式。
- MODE_STATIC：这种模式下，调用AudioTrack的`play()`函数之前，需要将所有的数据一次性write到AudioTrack的内部缓冲区，之后就不需要再传递数据了。这种模式适用于文件较小的时候（比如铃声），和对延时性要求比较高的时候。其缺点是一次write的数据不能太多，否则将会导致系统无法分配足够的内存来存储全部数据。

**AudioTrack实现MODE_STREAM的步骤如下：**

1. 计算系统根据硬件提供的最适合的最小bufferSize：

```kotlin
val bufferSize = AudioTrack.getMinBufferSize(
    44100,
    AudioFormat.CHANNEL_OUT_STEREO,//双声道
    AudioFormat.ENCODING_PCM_16BIT
)
```

2. 根据初始化的bufferSize生成程序的缓冲区：

```kot
data = ByteArray(bufferSize * 10)
```

3. 生成AudioTrack对象：

```kotlin
streamAudioTrack = AudioTrack(
    AudioManager.STREAM_MUSIC,
    44100,
    AudioFormat.CHANNEL_OUT_STEREO,
    AudioFormat.ENCODING_PCM_16BIT,
    data.size,
    AudioTrack.MODE_STREAM
)
```

AudioTrack的构造函数中，第一个参数为`streamType`，它的含义与Android系统对音频流的管理和分类有关。

Android将系统的声音分为好几种流类，下面是几种常见的类型：

- STREAM_ALARM：警告声
- STREAM_MUSIC：音乐声，例如music等
- STREAM_RING：铃声
- STREAM_SYSTEM：系统声音，例如低音提示音、锁屏音等
- STREAM_VOCIE_CALL：通话声

注意：上面这些类型的划分和音频数据本身并没有关系。例如MUSIC和RING类型都可以是某首MP3歌曲。声音流类型的选择没有固定标准，比如铃声预览中的铃声可以设置为MUSIC类型。音频流类型的划分只和Audio系统对音频的管理策略有关。

4. 播放音频，注意读取文件操作需要在子线程，防止干扰ui线程

```kotlin
private fun readAudioTrackStream() {
    //文件定位，用于实现暂停继续功能
    randomAccessFile.seek(totalCount)
    var readCount = 0
    while (readCount != -1) {
        readCount = randomAccessFile.read(data)
        totalCount += readCount
        if (readCount == AudioTrack.ERROR_BAD_VALUE || readCount == AudioTrack.ERROR_INVALID_OPERATION) continue
        if (readCount != 0 && readCount != -1) {
            if (isPause) {
                streamAudioTrack.pause()
                break
            } else {
                //播放时，将文件数据分批次写入AudioTrack
                streamAudioTrack.play()
                //写入时，程序阻塞，直到写入的数据都传输给AudioTrack
                streamAudioTrack.write(data, 0, readCount)
            }
        }
    }
}
```

5. 暂停功能：将标记的状态`isPause`设置为true，程序将退出读取文件循环
6. 重置功能：将标记的状态`isPause`设置为true，重置totalCount，再次点击播放将重新播放文件。

7. AudioTrack状态：通过`int getState()`函数获取当前状态

- STATE_INITIALIZED	当前在可用状态
- STATE_UNINITIALIZED    表示当前还未初始化
- STATE_NO_STATIC_DATA     表示当前使用的是MODE_STATIC模式，但是还没有往缓冲区中写入数据。当接收数据后，状态将改变成STATE_INITIALIZED

8. AudioTrack播放状态：通过`int getPlayState()`获取播放状态

- PLAYSTATE_STOPPED	停止
- PLAYSTATE_PAUSED     暂停
- PLAYSTATE_PLAYING     正在播放

9. 释放本地对象：调用`audioTrack.release()`释放占用资源

**AudioTrack与MediaPlayer对比**

1. 区别：最大的区别是MediaPlayer可以播放多种格式的声音文件，例如MP3，AAC，WAV，OGG，MIDI等。MediaPlayer会在framework层创建对应的音频解码器。而AudioTrack只能播放已经解码的PCM流，如果对比支持的格式的话则是AudioTrack只支持WAV格式的音频文件，因为WAV格式的音频文件大部分都是PCM流。AudioTrack不创建解码器，所以只能播放不需要解码的WAV文件
2. 联系：MediaPlayer在framework层还是会创建AudioTrack，把解码后的PCM数据流传递给AudioTrack，AudioTrack再传递给AudioFlinger进行混音，然后才传递给硬件播放，所以MediaPlayer包含了AudioStrack.
3. SoundPool：在接触Android音频播放API的时候，会发现SoundPool也可以用于播放音频。下面是三者的使用场景：MediaPlayer更加适合在后台长时间播放本地音乐文件或在线的流式资源；SoundPool则适合播放比较短的音频片段，比如游戏声音、按键音、铃声片段等，它可以同时播放多个音频；而AudioTrack则更接近底层，提供了非常强大的控制能力，支持低延迟播放，适合流媒体和VoIP语音电话等场景。

> 本章涉及到的类：`AudioTrackActivity`

