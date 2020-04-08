### Android音视频开发（二）：使用AudioRecord采集音频PCM并保存到文件

**一.	AudioRecord API 介绍**

AudioRecord是Android系统提供的用于实现录制音频的功能类，相对于MediaRecorder来说，AudioRecord更加接近底层，能够灵活自由地控制，可以得到原始的PCM音频数据。而MediaRecorder是一个更上层的API，它可以直接把手机麦克风录入的数据进行编码压缩（AMR、MP3等）并保存成文件。

因此，如果只是简单的想实现一个录音机，录制成音频文件，则推荐使用MediaRecorder，而如果需要对音频做进一步的算法处理、或者采集第三方的编码库进行压缩、以及网络传输等应用，则建议使用AudioRecord。

本质上，MediaRecorder底层也调用了AudioRecord与Android Framework层的AudioFlinger进行交互。

AudioRecord的官方解释为：

> AudioRecord类的只要功能是让各种JAVA应用能够管理音频资源，以便它们通过此类能够使用相关的硬件收集数据进行音频录制。此功能的实现就是通过`pulling`（读取）AudioRecord对象的声音数据来完成。在录音过程中，应用所需要做的就是通过AudioRecord获取录音数据。AudioRecord提供三个获取声音数据的方法分别是`read(byte[],int,int)`、`read(short[],int,int`、`read(ByteBuffer,int)`，无论选择哪一种都必须事先设定声音数据的存储格式。
>
> 开始录音的时候，AudioRecord需要初始化一个相关联的声音`buffer`（缓冲区），这个`buffer`主要用来保存新的声音数据。缓冲区的大小，在对象构造期间指定，它表明一个AudioRecord一次可以录制的声音容量。声音数据从音频硬件中被读出，数据大小不超过整个录音数据的大小，即每次读取初始化buffer容量的数据。

实现Android录音的流程为：

1. 构造一个AudioRecord对象，其中需要的最小录音缓存buffer大小可以通过`getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat)`函数获取，如果buffer容量过小，将导致对象构造失败
2. 初始化一个buffer，该buffer大于等于AudioRecord对象中过去的buffer大小
3. 调用`audioRecord.startRecording()`
4. 创建数据流，一边从AudioRecord中读取声音数据到初始化的buffer，一边将buffer中的数据存储到数据流
5. 调用`audioRecord.stop()`停止录音，调用`audioRecord.release()`释放音频资源

**二.	AudioRecord 录音流程**

1. 获取`recordBudderSize`：

   ```kotlin
   val recordBufferSize = AudioRecord.getMinBufferSize(
       44100,
       AudioFormat.CHANNEL_IN_DEFAULT,
       AudioFormat.ENCODING_PCM_16BIT
   )
   ```

   `AudioRecord.getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat)`需要三个参数：

   1. sampleRateInHz，采样率（赫兹），取值范围为4000-192000，在AudioFormat类里，`public static final int SAMPLE_RATE_HZ_MIN = 4000;`代表最小码率，`public static final int SAMPLE_RATE_HZ_MAX = 192000;`代表最大码率。

      采样率的简介

      > 音频采样率是指录音设备在一秒钟内对声音信号的采样次数，采样频率越高声音的还原就越真实越自然。在当今的主流采集卡上，采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级，22.05KHz只能达到FM广播的声音品质，44.1KHz则是理论上的CD音质界限，48KHz则更加精确一些。
      >
      > 在数字音频领域，常用的采样率有：
      >
      > 8,000 Hz - 电话所用采样率, 对于人的说话已经足够
      > 11,025 Hz
      >
      > 22,050 Hz - 无线电广播所用采样率
      >
      > 32,000 Hz - miniDV 数码视频 camcorder、DAT (LP mode)所用采样率
      >
      > 44,100 Hz - 音频 CD, 也常用于 MPEG-1 音频（VCD, SVCD, MP3）所用采样率
      >
      > 47,250 Hz - 商用 PCM 录音机所用采样率
      >
      > 48,000 Hz - miniDV、数字电视、DVD、DAT、电影和专业音频所用的数字声音所用采样率
      >
      > 50,000 Hz - 商用数字录音机所用采样率
      >
      > 96,000 或者 192,000 Hz - DVD-Audio、一些 LPCM DVD 音轨、BD-ROM（蓝光盘）音轨、和 HD-DVD （高清晰度 DVD）音轨所用所用采样率
      >
      > 2.8224 MHz - Direct Stream Digital 的 1 位 sigma-delta modulation 过程所用采样率。

      通常歌曲的采样率是44100，而Android平台的人声录音支持8000，16000，32000三种码率
      
   2. channelConfig，声道配置，描述音频声道的配置，例如左声道/右声道/左声道等
   
         声道在AudioFormat中具体参数有：
   
         >public static final int CHANNEL_IN_LEFT = 0x4;//左声道
         >
         >public static final int CHANNEL_IN_RIGHT = 0x8;//右声道
         >
         >public static final int CHANNEL_IN_FRONT = 0x10;//前声道
         >
         >public static final int CHANNEL_IN_BACK = 0x20;//后声道
         >
         >public static final int CHANNEL_IN_LEFT_PROCESSED = 0x40;
         >
         >public static final int CHANNEL_IN_RIGHT_PROCESSED = 0x80;
         >
         >public static final int CHANNEL_IN_FRONT_PROCESSED = 0x100;
         >
         >public static final int CHANNEL_IN_BACK_PROCESSED = 0x200;
         >
         >public static final int CHANNEL_IN_PRESSURE = 0x400;
         >
         >public static final int CHANNEL_IN_X_AXIS = 0x800;
         >
         >public static final int CHANNEL_IN_Y_AXIS = 0x1000;
         >
         >public static final int CHANNEL_IN_Z_AXIS = 0x2000;
         >
         >public static final int CHANNEL_IN_VOICE_UPLINK = 0x4000;
         >
         >public static final int CHANNEL_IN_VOICE_DNLINK = 0x8000;
         >
         >public static final int CHANNEL_IN_MONO = CHANNEL_IN_FRONT;//单声道
         >
         >public static final int CHANNEL_IN_STEREO = (CHANNEL_IN_LEFT | CHANNEL_IN_RIGHT);//立体声道(左右声道)
   
   3. audioFormat，音频格式，表示音频数据的格式，一般手机设备可能只支持16位PCM编码，如果其他的都会报错位坏值。
   
2. 生成AudioRecord对象：

   ```kotlin
   audioRecord = AudioRecord(
           MediaRecorder.AudioSource.MIC,//音频源，这里选择麦克风
           44100,//码率，应与recordBufferSize中的一致
           AudioFormat.CHANNEL_IN_DEFAULT,//声道，应与recordBufferSize中的一致
           AudioFormat.ENCODING_PCM_16BIT,//音频格式，应与recordBufferSize中的一致
           recordBufferSize//缓存区大小
       )
   ```
   
3. 初始化用于缓存数据的ByteArray数组

   ```kotlin
   data = ByteArray(recordBufferSize)
   ```
   
4. 初始化用于缓存数据的ByteArray数组
   
   创建一个数据流，从AudioRecord中读取数据，并保存到文件中，注意文件读写的进程应该在子线程中读取，防止占用UI线程：
   
   ```kotlin
   while (isRecording) {
       if (data == null || recordBufferSize == null || audioRecord == null) return
       val read = audioRecord!!.read(data!!, 0, recordBufferSize!!)
       if (AudioRecord.ERROR_INVALID_OPERATION != read) {
           FileIOUtils.writeFileFromBytesByStream(file, data, true)
       }
   }
   ```
   
5. 生成PCM文件后，由于PCM格式的文件Android系统不支持播放，因此将PCM转化为WAV文件：
   
   ```kotlin
   PcmToWavUtil.convertPcm2Wav(file.absolutePath,outFile.absolutePath,44100,AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT)
   ```

**三、PCM转WAV格式**

在Android平台上要进行音频编辑操作（比如裁剪，插入，合成等），通常都是需要将音频文件解码为WAV格式的音频文件或者PCM文件。那么WAV和PCM之间有什么关系，这里有必要了解一下。

> PCM（Pulse Code Modulation—-脉码调制录音)。所谓PCM录音就是将声音等模拟信号变成符号化的脉冲列，再予以记录。PCM信号是由[1]、[0]等符号构成的数字信号，而未经过任何编码和压缩处理。与模拟信号比，它不易受传送系统的杂波及失真的影响。动态范围宽，可得到音质相当好的影响效果。也就是说，PCM就是没有压缩的编码方式，PCM文件就是采用PCM这种没有压缩的编码方式编码的音频数据文件。
>
> WAV是由微软开发的一种音频格式。WAV符合 PIFF Resource Interchange File Format规范。所有的WAV都有一个文件头，这个文件头音频流的编码参数。WAV对音频流的编码没有硬性规定，除了PCM之外，还有几乎所有支持ACM规范的编码都可以为WAV的音频流进行编码。WAV也可以使用多种音频编码来压缩其音频流，不过我们常见的都是音频流被PCM编码处理的WAV，但这不表示WAV只能使用PCM编码，MP3编码同样也可以运用在WAV中，和AVI一样，只要安装好了相应的Decode，就可以欣赏这些WAV了。
>
> 在Windows平台下，基于PCM编码的WAV是被支持得最好的音频格式，所有音频软件都能完美支持，由于本身可以达到较高的音质的要求，因此，WAV也是音乐编辑创作的首选格式，适合保存音乐素材。因此，基于PCM编码的WAV被作为了一种中介的格式，常常使用在其他编码的相互转换之中，例如MP3转换成WMA。

也就是说，PCM是单纯的音频数据，所以无法播放，要使音频播放就必须在PCM数据的基础上添加一组头信息，WAV文件头信息由大小44个字节的数据组成：

> 4字节数据，内容为“RIFF”，表示资源交换文件标识
>
> 4字节数据，内容为一个整数，表示从下个地址开始到文件尾的总字节数
>
> 4字节数据，内容为“WAVE”，表示WAV文件标识
>
> 4字节数据，内容为“fmt ”，表示波形格式标识（fmt ），最后一位空格。
>
> 4字节数据，内容为一个整数，表示PCMWAVEFORMAT的长度
>
> 2字节数据，内容为一个短整数，表示格式种类（值为1时，表示数据为线性PCM编码）
>
> 2字节数据，内容为一个短整数，表示通道数，单声道为1，双声道为2
>
> 4字节数据，内容为一个整数，表示采样率，比如44100
>
> 4字节数据，内容为一个整数，表示波形数据传输速率（每秒平均字节数），大小为 采样率 * 通道数 * 采样位数
>
> 2字节数据，内容为一个短整数，表示DATA数据块长度，大小为 通道数 * 采样位数
>
> 2字节数据，内容为一个短整数，表示采样位数，即PCM位宽，通常为8位或16位
>
> 4字节数据，内容为“data”，表示数据标记符
>
> 4字节数据，内容为一个整数，表示接下来声音数据的总大小

PCM转WAV格式的代码为：

```kotlin
object PcmToWavUtil {
    /**
     * PCM文件转WAV文件
     * @param inPcmFilePath 输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate 采样率，例如44100
     * @param channels 声道数 单声道：1或双声道：2
     * @param bitNum 采样位数，8或16
     */
    fun convertPcm2Wav(
        inPcmFilePath: String, outWavFilePath: String, sampleRate: Int,
        channels: Int, bitNum: Int
    ) {
        var `in`: FileInputStream? = null
        var out: FileOutputStream? = null
        val data = ByteArray(1024)
        try {
            //采样字节byte率
            val byteRate = sampleRate * channels * bitNum / 8.toLong()
            `in` = FileInputStream(inPcmFilePath)
            out = FileOutputStream(outWavFilePath)

            //PCM文件大小
            val totalAudioLen: Long = `in`.channel.size()

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            val totalDataLen = totalAudioLen + 36
            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate)
            var length = 0
            while (`in`.read(data).also { length = it } > 0) {
                out.write(data, 0, length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (out != null) {
                try {
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 输出WAV文件
     * @param out WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen 整个数据大小
     * @param sampleRate 采样率
     * @param channels 声道数
     * @param byteRate 采样字节byte率
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long, sampleRate: Int, channels: Int, byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.toByte() // RIFF
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte() //数据大小
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte() //WAVE
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        //FMT Chunk
        header[12] = 'f'.toByte() // 'fmt '
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte() //过渡字节
        //数据大小
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        //编码方式 10H为PCM编码格式
        header[20] = 1 // format = 1
        header[21] = 0
        //通道数
        header[22] = channels.toByte()
        header[23] = 0
        //采样率，每个通道的播放速度
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (channels * 16 / 8).toByte()
        header[33] = 0
        //每个样本的数据位数
        header[34] = 16
        header[35] = 0
        //Data chunk
        header[36] = 'd'.toByte() //data
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }
}
```

> 本章涉及类：`PcmToWavUtil`、`AudioRecordActivity`

