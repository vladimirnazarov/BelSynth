package com.ssrlab.assistant.utils.vm

import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.MediaRecorder
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.MessageClient
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.ui.chat.*
import com.ssrlab.assistant.utils.AUDIO_FORMAT
import com.ssrlab.assistant.utils.CHANNEL_CONFIG
import com.ssrlab.assistant.utils.SAMPLE_RATE
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.*
import java.io.File

class ChatViewModel : ViewModel() {

    private lateinit var audioRecord: AudioRecord
    private var isRecording = false

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun startRecording(binding: ActivityMainBinding, mainActivity: MainActivity) {
        startRecordingFunctionality(binding, mainActivity)
    }

    fun controlBottomVisibility(mainActivity: MainActivity, binding: ActivityMainBinding, hideBottom: Boolean = true) {
        if (!isRecording) {
            if (hideBottom) {
                binding.apply {
                    mainActivity.runOnUiThread {
                        mainChatMsgHolder.visibility = View.VISIBLE
                        mainBottomBar.visibility = View.GONE
                        mainRecordButton.visibility = View.GONE
                    }
                }
            } else {
                scope.launch {
                    delay(50)

                    mainActivity.runOnUiThread {
                        binding.apply {
                            mainChatMsgHolder.visibility = View.GONE
                            mainBottomBar.visibility = View.VISIBLE
                            mainRecordButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    fun sendMessage(text: String? = null, audio: File? = null, speaker: String, role: String = "assistant", mainActivity: MainActivity) {

        val botText = MutableLiveData<String>()
        var botAudio = ""

        if (text != null && audio == null) {
            scope.launch {
                MessageClient.sendMessage(text, speaker, role, { responseAudio, responseText ->
                    mainActivity.runOnUiThread {
                        botAudio = responseAudio
                        botText.value = responseText
                    }
                }) {
                    mainActivity.runOnUiThread {
                        Toast.makeText(mainActivity, "$it, try again", Toast.LENGTH_SHORT).show()
                        mainActivity.hideLoadingUtils()
                    }
                }
            }
        } else if (text == null && audio != null) {
            scope.launch {
                MessageClient.sendMessage(audio, speaker, role, { responseAudio, responseText ->
                    mainActivity.runOnUiThread {
                        botAudio = responseAudio
                        botText.value = responseText
                    }
                }) {
                    mainActivity.runOnUiThread {
                        Toast.makeText(mainActivity, "$it, try again", Toast.LENGTH_SHORT).show()
                        mainActivity.hideLoadingUtils()
                    }
                }
            }
        }

        botText.observe(mainActivity) {
            mainActivity.apply {
                loadBotMessage(it, botAudio)
                hideLoadingUtils()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecordingFunctionality(binding: ActivityMainBinding, mainActivity: MainActivity) {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBufferSize
        )

        audioRecord.startRecording()
        isRecording = true

        binding.apply {
            val alphaInAnim = AnimationUtils.loadAnimation(mainActivity, R.anim.alpha_in)

            mainWaveLayout.startAnimation(alphaInAnim)
            mainWaveCenter.startAnimation(alphaInAnim)
            mainDurationHolder.startAnimation(alphaInAnim)

            mainWaveLayout.visibility = View.VISIBLE
            mainWaveCenter.visibility = View.VISIBLE
            mainDurationHolder.visibility = View.VISIBLE
            mainWaveReplacement.visibility = View.GONE
        }

        scope.launch {
            var currentTime = 0

            while (isRecording) {
                mainActivity.runOnUiThread { binding.mainDurationText.text = mainActivity.getChatHelper().convertToTimeMillis(currentTime) }

                delay(1000)
                currentTime += 1000
            }
        }

        Thread {
            val bufferSize = 1024
            val buffer = ShortArray(bufferSize)
            val fftData = FloatArray(bufferSize)

            while (isRecording) {

                val bytesRead = audioRecord.read(buffer, 0, bufferSize)
                for (i in 0 until bytesRead) fftData[i] = buffer[i].toFloat() / Short.MAX_VALUE

                val fft = FloatFFT_1D(bufferSize)
                fft.realForward(fftData)

                mainActivity.getVisualizerView().updateFFTData(fftData)
            }
        }.start()
    }

    fun isRecording() = isRecording
    fun setIsRecording(value: Boolean) { isRecording = value }
    fun getAudioRecord() = audioRecord
}