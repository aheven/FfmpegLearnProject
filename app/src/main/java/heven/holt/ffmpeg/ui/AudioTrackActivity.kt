package heven.holt.ffmpeg.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.*
import heven.holt.ffmpeg.R
import java.io.File
import java.io.RandomAccessFile

class AudioTrackActivity : AppCompatActivity() {
    companion object {
        fun startAtc() {
            ActivityUtils.startActivity(AudioTrackActivity::class.java)
        }
    }

    private lateinit var data: ByteArray
    private lateinit var streamAudioTrack: AudioTrack
    private lateinit var staticAudioTrack: AudioTrack
    private var isPause = false

    private val outFile = File(PathUtils.getExternalAppCachePath(), "record.wav")
    private var staticData: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_track)

        val bufferSize = AudioTrack.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_OUT_STEREO,//双声道
            AudioFormat.ENCODING_PCM_16BIT
        )
        data = ByteArray(bufferSize * 10)
        streamAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT,
            data.size,
            AudioTrack.MODE_STREAM
        )

        initStaticModeData()
    }

    private fun initStaticModeData() {
        staticData = FileIOUtils.readFile2BytesByStream(outFile)
        createStaticAudioTrack()
    }

    private fun createStaticAudioTrack() {
        staticAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT,
            staticData!!.size,
            AudioTrack.MODE_STATIC
        )
    }

    fun clickButton(view: View) {
        when (view.id) {
            R.id.stream_play -> {
                isPause = false
                ThreadUtils.executeBySingle(object : ThreadUtils.Task<Any>() {
                    override fun doInBackground(): Any {
                        readAudioTrackStream()
                        return Any()
                    }

                    override fun onSuccess(result: Any?) {
                    }

                    override fun onFail(t: Throwable?) {
                    }

                    override fun onCancel() {
                    }
                })
            }
            R.id.stream_pause -> {
                isPause = true
            }
            R.id.stream_reset -> {
                isPause = true
                totalCount = 0
            }
            R.id.static_play -> {
                if (staticData == null) {
                    ToastUtils.showShort("数据未初始化完成")
                    return
                }
                if (staticAudioTrack.state == AudioTrack.STATE_UNINITIALIZED) {
                    createStaticAudioTrack()
                }
                staticAudioTrack.write(staticData!!, 0, staticData!!.size)
                staticAudioTrack.play()
            }
            R.id.static_pause -> {
                staticAudioTrack.pause()
            }
            R.id.static_reset -> {
                staticAudioTrack.release()
            }
        }
    }

    private val randomAccessFile = RandomAccessFile(outFile, "r")
    private var totalCount = 0L

    private fun readAudioTrackStream() {
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
                    streamAudioTrack.play()
                    streamAudioTrack.write(data, 0, readCount)
                }
            }
        }
    }
}
