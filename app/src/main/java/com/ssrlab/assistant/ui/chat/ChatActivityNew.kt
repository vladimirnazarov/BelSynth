package com.ssrlab.assistant.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.chat.ChatMessagesClient
import com.ssrlab.assistant.client.chat.ChatsInfoClient
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.db.objects.messages.Message
import com.ssrlab.assistant.rv.ChatAdapterNew
import com.ssrlab.assistant.utils.AUDIO_FORMAT
import com.ssrlab.assistant.utils.BOT
import com.ssrlab.assistant.utils.CHANNEL_CONFIG
import com.ssrlab.assistant.utils.CHAT_ID
import com.ssrlab.assistant.utils.CHAT_IMAGE
import com.ssrlab.assistant.utils.CHAT_NAME
import com.ssrlab.assistant.utils.CHAT_ROLE
import com.ssrlab.assistant.utils.CHAT_ROLE_CODE
import com.ssrlab.assistant.utils.CHAT_ROLE_INT
import com.ssrlab.assistant.utils.CHAT_SPEAKER
import com.ssrlab.assistant.utils.NULL
import com.ssrlab.assistant.utils.PERMISSIONS_REQUEST_CODE
import com.ssrlab.assistant.utils.SAMPLE_RATE
import com.ssrlab.assistant.utils.USER
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.InAppReviewer
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObjectNew
import com.ssrlab.assistant.utils.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModelNew
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class ChatActivityNew: BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView
    private lateinit var chatsInfoClient: ChatsInfoClient
    private lateinit var chatMessagesClient: ChatMessagesClient

    private val viewModel: ChatViewModelNew by viewModels {
        ChatViewModelNew.Factory(this@ChatActivityNew)
    }

    private lateinit var audioRecord: AudioRecord
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false

    private lateinit var imm: InputMethodManager
    private var originalScreenHeight = 0

    private var playableValue = true
    private var chatId = NULL
    private var name = NULL
    private var speaker = NULL
    private var role = NULL
    private var roleCode = NULL
    private var roleInt = 0

    private lateinit var audioFile: File
    private lateinit var adapter: ChatAdapterNew

    private var messages = arrayListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        mainApp.setContext(this@ChatActivityNew)
        mainApp.loadPreferences(sharedPreferences)

        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatsInfoClient = ChatsInfoClient(this@ChatActivityNew)
        chatMessagesClient = ChatMessagesClient(this@ChatActivityNew)
        chatHelper = ChatHelper()
    }

    override fun onStart() {
        super.onStart()

        setupChat()
        setupFFTLayout()
        setupKeyBoardAction()
        setupRecordButton()
        setupMessageSendButton()
    }

    override fun onStop() {
        super.onStop()

        saveData()
        MediaPlayerObjectNew.pauseAudio(adapter)
    }

    private fun setupChat() {
        getIntents()

        setupAdapter()

        playableValue = sharedPreferences.getBoolean("playable_${speaker}_${roleCode}", true)
        viewModel.apply {
            setupAudioButton(adapter)
            setupToolbar(name, role, speaker)
            setPlayable(playableValue)
        }
    }

    private fun setupRecordButton() {
        binding.chatRecordRipple.setOnClickListener {
            if (!isRecording) {
                if (checkPermissions()) {
                    val audioPath = File("$cacheDir/chats/${speaker}_${roleCode}")
                    if (!audioPath.exists()) audioPath.mkdirs()
                    audioFile = File(audioPath, "user_temp.mp4")

                    MediaPlayerObjectNew.pauseAudio(adapter = adapter)

                    startRecording(audioFile)
                    binding.chatRecordImage.setImageResource(R.drawable.ic_mic_off)
                    binding.chatKeyboardButton.isClickable = false
                }
            } else {
                stopRecording()

                binding.chatRecordImage.setImageResource(R.drawable.ic_mic_on)
                binding.chatKeyboardButton.isClickable = true

                uploadAudio(audioFile)
            }
        }
    }

    private fun setupMessageSendButton() {
        binding.apply {
            chatChatMsgSend.setOnClickListener {
                if (chatChatMsgInput.text?.isNotEmpty() == true) {
                    messages.add(Message(chatChatMsgInput.text!!.toString(), USER))
                    viewModel.updateAdapter(adapter, messages)

                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                    sendMessage(text = chatChatMsgInput.text?.toString()?.replace("\n", ". ").toString())
                    chatChatMsgInput.text?.clear()
                }
            }
        }
    }

    private fun setupKeyBoardAction() {
        binding.apply {
            chatKeyboardButton.setOnClickListener {
                imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                chatChatMsgInput.requestFocus()
                imm.showSoftInput(chatChatMsgInput, InputMethodManager.SHOW_IMPLICIT)
            }

            val keyboardVisibilityListener = ViewTreeObserver.OnGlobalLayoutListener {
                if (originalScreenHeight == 0) originalScreenHeight = chatView.height
                val screenHeight = chatView.height

                if (originalScreenHeight != screenHeight) {
                    viewModel.controlBottomVisibility()
                    chatChatRv.smoothScrollToPosition(adapter.itemCount - 1)
                } else {
                    viewModel.apply {
                        controlBottomVisibility(false)
                    }
                }
            }

            chatView.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityListener)
        }
    }

    private fun setupFFTLayout() {
        visualizerView = FFTVisualizerView(this, null)
        val fftLayout = binding.chatWaveLayout
        fftLayout.addView(visualizerView)
    }

    private fun setupAdapter() {
        binding.apply {
            if (messages.isEmpty()) {
                //TODO
                val firstMessage = viewModel.generateFirstMessage(roleInt)
                messages.add(firstMessage)
            }

            adapter = ChatAdapterNew(messages, this@ChatActivityNew, chatMessagesClient)

            chatChatRv.layoutManager = LinearLayoutManager(this@ChatActivityNew)
            chatChatRv.adapter = adapter
            chatChatRv.smoothScrollToPosition(adapter.itemCount)
        }
    }

    private fun startRecording(outputFile: File) {
        setupAudioRecorder()
        setupMediaRecorder(outputFile)

        audioRecord.startRecording()
        mediaRecorder.start()
        isRecording = true

        val runtime = Runtime.getRuntime()
        val availableProcessors = runtime.availableProcessors()
        if (availableProcessors >= 3) {
            viewModel.showWave()
            startWaveThread()
        }

        viewModel.startTimer()
    }

    private fun stopRecording() {
        if (isRecording) {
            this.apply {
                audioRecord.stop()
                mediaRecorder.stop()

                audioRecord.release()
                mediaRecorder.release()
                isRecording = false
            }

            viewModel.hideTimerAndWave()
        }
    }

    /**
     * For FFT Thread
     */
    @SuppressLint("MissingPermission")
    private fun setupAudioRecorder() {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBufferSize
        )
    }

    /**
     * For common voice record
     */
    private fun setupMediaRecorder(outputFile: File) {
        createMediaRecorder(this).apply {
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
    }

    private fun sendMessage(text: String = NULL, audio: String = NULL) {
        chatHelper.showLoadingUtils(binding, this@ChatActivityNew)
        chatMessagesClient.sendMessage(chatId, text, audio, {
            runOnUiThread {
                messages.add(it)
                viewModel.updateAdapter(adapter, messages)
                chatHelper.hideLoadingUtils(binding)

                if (viewModel.getPlayable()) adapter.playAudio(link = it.audio)
                runOnUiThread { checkIfAppRated() }
            }
        }, {
            viewModel.showErrorMessage(it)
        })
    }

    private fun uploadAudio(audioFile: File) {
        chatHelper.showLoadingUtils(binding, this@ChatActivityNew)
        chatMessagesClient.uploadAudio(audioFile, {
            runOnUiThread {
                chatHelper.hideLoadingUtils(binding)
                messages.add(Message(NULL, USER, it))
                viewModel.updateAdapter(adapter, messages)

                sendMessage(audio = it)
            }
        }, {
            viewModel.showErrorMessage(it)
        })
    }

    private fun createMediaRecorder(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
    }

    private fun startWaveThread() {
        Thread {
            val bufferSize = 1024
            val buffer = ShortArray(bufferSize)
            val fftData = FloatArray(bufferSize)

            while (isRecording) {
                val bytesRead = audioRecord.read(buffer, 0, bufferSize)
                for (i in 0 until bytesRead) fftData[i] = buffer[i].toFloat() / Short.MAX_VALUE

                val fft = FloatFFT_1D(bufferSize)
                fft.realForward(fftData)

                runOnUiThread {
                    visualizerView.updateFFTData(fftData)
                }
            }
        }.start()
    }

    private fun checkIfAppRated() {
        var identifierForRateView = 1
        for (i in messages)
            if (i.role == BOT)
                identifierForRateView++
        if (identifierForRateView == 5 && !mainApp.isUserRated()) {
            viewModel.launch {
                delay(4000)
                InAppReviewer().askUserForReview(this@ChatActivityNew) {
                    mainApp.saveIsUserRated(sharedPreferences)
                }
            }
        }
    }

    private fun getIntents() {
        intent.apply {
            name = getStringExtra(CHAT_NAME) ?: NULL
            speaker = getStringExtra(CHAT_SPEAKER) ?: NULL
            role = getStringExtra(CHAT_ROLE) ?: NULL
            roleCode = getStringExtra(CHAT_ROLE_CODE) ?: NULL
            roleInt = getIntExtra(CHAT_ROLE_INT, 0)
            chatId = getStringExtra(CHAT_ID) ?: NULL

            binding.chatToolbarImage.setImageResource(getIntExtra(CHAT_IMAGE, R.drawable.img_speaker_1))
        }
    }

    private fun saveData() {
        sharedPreferences.edit().apply {
            putBoolean("playable_${speaker}_${roleCode}", viewModel.getPlayable())
            apply()
        }
    }

    fun shareIntent(text: String) {
        val finalText = "${resources.getText(R.string.share_text_1)} $speaker ${resources.getText(R.string.share_text_2)} ${resources.getText(R.string.app_name)}:\n\n$text"

        ShareCompat.IntentBuilder(this@ChatActivityNew)
            .setChooserTitle(resources.getText(R.string.share_using))
            .setType("text/plain")
            .setText(finalText)
            .startChooser()
    }

    private fun checkPermissions() : Boolean {
        if (ContextCompat.checkSelfPermission(this@ChatActivityNew, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ChatActivityNew, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_CODE)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val audioPath = File("$cacheDir/chats/${speaker}_${roleCode}")
                if (!audioPath.exists()) audioPath.mkdirs()
                audioFile = File(audioPath, "user_temp.mp4")

                MediaPlayerObjectNew.pauseAudio(adapter = adapter)

                startRecording(audioFile)
                binding.chatRecordImage.setImageResource(R.drawable.ic_mic_off)
                binding.chatKeyboardButton.isClickable = false
            }
        }
    }

    fun getBinding() = binding
    fun isRecording() = isRecording
    fun getHelper() = chatHelper
    fun getChatViewModel() = viewModel
}