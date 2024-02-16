package com.ssrlab.assistant.utils.vm

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.chat.MessageClient
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.db.objects.BotMessage
import com.ssrlab.assistant.ui.chat.*
import com.ssrlab.assistant.utils.AUDIO_FORMAT
import com.ssrlab.assistant.utils.CHANNEL_CONFIG
import com.ssrlab.assistant.utils.SAMPLE_RATE
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject.initializeMediaPlayer
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject.pauseAudio
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject.playAudio
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class ChatViewModel : ViewModel() {

    private lateinit var audioRecord: AudioRecord
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false

    var playable = MutableLiveData<Boolean>()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun generateFirstMessage(id: Int, roleInt: Int, activity: ChatActivity) : BotMessage {
        var botMessage = BotMessage(id, text = "Вітаю! Чым я магу дапамагчы?")
        when (roleInt) {
            1 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_1)) }
            2 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_2)) }
            3 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_3)) }
            4 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_4)) }
            5 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_5)) }
            6 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_6)) }
            7 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_7)) }
            8 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_8)) }
            9 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_9)) }
            10 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_10)) }
            11 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_11)) }
            12 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_12)) }
            13 -> { botMessage = BotMessage(id, activity.resources.getString(R.string.role_additional_13)) }
        }

        return botMessage
    }

    fun startRecording(chatActivity: ChatActivity, outputFile: File) {
        startRecordingFunctionality(chatActivity, outputFile)
    }

    fun stopRecording(binding: ActivityChatBinding) {
        if (isRecording()) {
            this.apply {
                audioRecord.stop()
                mediaRecorder.stop()

                audioRecord.release()
                mediaRecorder.release()
                isRecording = false
            }

            binding.apply {
                chatWaveLayout.visibility = View.GONE
                chatWaveCenter.visibility = View.GONE
                chatDurationHolder.visibility = View.GONE
                chatWaveReplacement.visibility = View.VISIBLE
            }
        }
    }

    fun sendMessage(text: String, speaker: String, role: String, chatActivity: ChatActivity) {
        val botAudio = MutableLiveData<File>()
        var botText = ""

        scope.launch {
            MessageClient.sendMessage(text, speaker, role, { responseAudioLink, responseText ->
                chatActivity.runOnUiThread {
                    botText = responseText
                    loadAudioFile(responseAudioLink, chatActivity) {
                        chatActivity.runOnUiThread { botAudio.value = it }
                    }
                }
            }) { showErrorMessage(it, chatActivity) }
        }

        botAudio.observe(chatActivity) {
            chatActivity.apply {
                loadBotMessage(botText, it)

                if (playable.value!!) {
                    pauseAudio()
                    initializeMediaPlayer(chatActivity, it.toUri())
                    playAudio()
                }

                ChatHelper().hideLoadingUtils(chatActivity.getBinding())
            }
        }
    }

    fun sendMessage(audio: File, speaker: String, role: String, chatActivity: ChatActivity) {
        val botAudio = MutableLiveData<File>()
        var botText = ""

        scope.launch {
            MessageClient.sendMessage(audio, speaker, role, { responseAudioLink, responseText ->
                chatActivity.runOnUiThread {
                    botText = responseText
                    loadAudioFile(responseAudioLink, chatActivity) {
                        chatActivity.runOnUiThread { botAudio.value = it }
                    }
                }
            }) {
                MessageClient.sendMessage(audio, speaker, role, { responseAudioLink, responseText ->
                    chatActivity.runOnUiThread {
                        botText = responseText
                        loadAudioFile(responseAudioLink, chatActivity) {
                            chatActivity.runOnUiThread { botAudio.value = it }
                        }
                    }
                }) {
                    showErrorMessage(it, chatActivity)
                }
            }
        }

        botAudio.observe(chatActivity) {
            chatActivity.apply {
                loadBotMessage(botText, it)

                if (playable.value!!) {
                    pauseAudio()
                    initializeMediaPlayer(chatActivity, it.toUri())
                    playAudio()
                }

                ChatHelper().hideLoadingUtils(chatActivity.getBinding())
            }
        }
    }

    private fun loadAudioFile(link: String, chatActivity: ChatActivity, onSuccess: (File) -> Unit) {
        val file = File(chatActivity.getExternalFilesDir(null), "bv_msg_${chatActivity.getId()}_${chatActivity.getSpeaker()}.mp3")
        MessageClient.getAudio(link, file, {
            onSuccess(file)
        }) {
            chatActivity.runOnUiThread {
                Toast.makeText(chatActivity, "$it, error", Toast.LENGTH_SHORT).show()
                onSuccess(file)
            }
        }
    }

    private fun showErrorMessage(msg: String, chatActivity: ChatActivity) {
        chatActivity.runOnUiThread {
            Toast.makeText(chatActivity, "$msg, try again", Toast.LENGTH_SHORT).show()
            ChatHelper().hideLoadingUtils(chatActivity.getBinding())
        }
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(context: Context) : MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    @SuppressLint("MissingPermission")
    private fun startRecordingFunctionality(chatActivity: ChatActivity, outputFile: File) {

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBufferSize
        )

        createMediaRecorder(chatActivity).apply {
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setAudioSamplingRate(44100)
            setAudioChannels(1)
            setAudioEncodingBitRate(128000)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()

            mediaRecorder = this
        }

        audioRecord.startRecording()
        mediaRecorder.start()
        isRecording = true

        val runtime = Runtime.getRuntime()
        val availableProcessors = runtime.availableProcessors()
        if (availableProcessors >= 3) {
            showWave(chatActivity)
            startWaveThread(chatActivity)
        }
        startTimer(chatActivity)
    }

    private fun startTimer(chatActivity: ChatActivity) {
        chatActivity.getBinding().apply {
            val alphaInAnim = AnimationUtils.loadAnimation(chatActivity, R.anim.alpha_in)
            chatDurationHolder.startAnimation(alphaInAnim)
            chatDurationHolder.visibility = View.VISIBLE
        }

        scope.launch {
            var currentTime = 0

            while (isRecording) {
                chatActivity.runOnUiThread { chatActivity.getBinding().chatDurationText.text = chatActivity.getChatHelper().convertToTimerMode(currentTime) }

                delay(1000)
                currentTime += 1000
            }
        }
    }

    private fun startWaveThread(chatActivity: ChatActivity) {
        Thread {
            val bufferSize = 1024
            val buffer = ShortArray(bufferSize)
            val fftData = FloatArray(bufferSize)

            while (isRecording) {

                val bytesRead = audioRecord.read(buffer, 0, bufferSize)
                for (i in 0 until bytesRead) fftData[i] = buffer[i].toFloat() / Short.MAX_VALUE

                val fft = FloatFFT_1D(bufferSize)
                fft.realForward(fftData)

                chatActivity.apply {
                    runOnUiThread {
                        getVisualizerView().updateFFTData(fftData)
                    }
                }
            }
        }.start()
    }

    fun controlBottomVisibility(chatActivity: ChatActivity, hideBottom: Boolean = true) {
        if (!isRecording) {
            if (hideBottom) {
                chatActivity.getBinding().apply {
                    chatActivity.runOnUiThread {
                        chatChatMsgHolder.visibility = View.VISIBLE
                        chatBottomBar.visibility = View.GONE
                        chatRecordButton.visibility = View.GONE
                    }
                }
            } else {
                scope.launch {
                    delay(50)

                    chatActivity.runOnUiThread {
                        chatActivity.getBinding().apply {
                            chatChatMsgHolder.visibility = View.GONE
                            chatBottomBar.visibility = View.VISIBLE
                            chatRecordButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun showWave(chatActivity: ChatActivity) {
        chatActivity.getBinding().apply {
            val alphaInAnim = AnimationUtils.loadAnimation(chatActivity, R.anim.alpha_in)

            chatWaveLayout.startAnimation(alphaInAnim)
            chatWaveCenter.startAnimation(alphaInAnim)

            chatWaveLayout.visibility = View.VISIBLE
            chatWaveCenter.visibility = View.VISIBLE
            chatWaveReplacement.visibility = View.GONE
        }
    }

    fun isRecording() = isRecording
}