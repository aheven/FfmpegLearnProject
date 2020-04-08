package heven.holt.ffmpeg.utils

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 *Time:2020/4/8
 *Author:HevenHolt
 *Description:
 */
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