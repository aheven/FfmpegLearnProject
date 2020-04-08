package heven.holt.ffmpeg.ui

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.*
import heven.holt.ffmpeg.R
import heven.holt.ffmpeg.utils.PcmToWavUtil
import java.io.File

class AudioRecordActivity : AppCompatActivity() {
    companion object {
        fun startAtc() {
            ActivityUtils.startActivity(AudioRecordActivity::class.java)
        }
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var data: ByteArray? = null
    private var recordBufferSize: Int? = null
    private val file = File(PathUtils.getExternalAppCachePath(), "record.pcm")
    private val outFile = File(PathUtils.getExternalAppCachePath(), "record.wav")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        recordBufferSize = createAudioRecord()

        LogUtils.e(recordBufferSize)
    }

    private fun createAudioRecord(): Int {
        val recordBufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT,
            recordBufferSize
        )
        data = ByteArray(recordBufferSize)
        return recordBufferSize
    }

    fun clickButton(view: View) {
        when (view.id) {
            R.id.record_start -> {
                if (audioRecord == null) {
                    recordBufferSize = createAudioRecord()
                }
                audioRecord?.startRecording()
                isRecording = true
                ThreadUtils.executeBySingle(object : ThreadUtils.Task<Any>() {
                    override fun doInBackground(): Any {
                        saveRecordData()
                        return Any()
                    }

                    override fun onSuccess(result: Any?) {
                        PcmToWavUtil.convertPcm2Wav(file.absolutePath,outFile.absolutePath,44100,AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT)
                    }

                    override fun onFail(t: Throwable?) {
                    }

                    override fun onCancel() {
                    }
                })
            }
            R.id.record_stop -> {
                isRecording = false
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            }
        }
    }

    /**
     * 保存录制数据
     */
    private fun saveRecordData() {
        FileUtils.delete(file)
        while (isRecording) {
            if (data == null || recordBufferSize == null || audioRecord == null) return
            val read = audioRecord!!.read(data!!, 0, recordBufferSize!!)
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                FileIOUtils.writeFileFromBytesByStream(file, data, true)
            }
        }
    }
}
