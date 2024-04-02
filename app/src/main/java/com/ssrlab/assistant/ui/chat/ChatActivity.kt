package com.ssrlab.assistant.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.chat.ChatMessagesClient
import com.ssrlab.assistant.client.chat.ChatsInfoClient
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.databinding.DialogLoadingBinding
import com.ssrlab.assistant.db.client.DatabaseClient
import com.ssrlab.assistant.db.objects.Message
import com.ssrlab.assistant.rv.ChatAdapter
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
import com.ssrlab.assistant.utils.helpers.text.ChatHelper
import com.ssrlab.assistant.utils.helpers.other.InAppReviewer
import com.ssrlab.assistant.utils.helpers.other.MainMediaPlayer
import com.ssrlab.assistant.utils.helpers.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModel
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class ChatActivity: BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView
    private lateinit var chatsInfoClient: ChatsInfoClient
    private lateinit var chatMessagesClient: ChatMessagesClient
    private lateinit var database: DatabaseClient

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModel.Factory(this@ChatActivity)
    }

    private lateinit var audioRecord: AudioRecord
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private var playableValue = true

    private lateinit var imm: InputMethodManager
    private var originalScreenHeight = 0

    private var chatId = NULL
    private var name = NULL
    private var speaker = NULL
    private var role = NULL
    private var roleCode = NULL
    private var roleInt = 0

    private lateinit var audioFile: File
    private lateinit var adapter: ChatAdapter
    private lateinit var mediaPlayer: MainMediaPlayer

    private var messages = arrayListOf<Message>()
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        mainApp.setContext(this@ChatActivity)
        mainApp.loadPreferences(sharedPreferences)

        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatsInfoClient = ChatsInfoClient(this@ChatActivity)
        chatMessagesClient = ChatMessagesClient(this@ChatActivity)
        chatHelper = ChatHelper()
        database = DatabaseClient(this@ChatActivity)
        mediaPlayer = MainMediaPlayer()
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

        savePlayable()
        mediaPlayer.pauseAudio(adapter)
    }

    private fun getMessages(onSuccess: (ArrayList<Message>) -> Unit, onFailure: () -> Unit) {
        val messagesObservable = MutableLiveData<ArrayList<Message>?>()
        messagesObservable.observe(this@ChatActivity) {
            if (it == null) {
                onFailure()
            } else {
                onSuccess(it)
            }
        }

        viewModel.apply {
            launch {
                val value = database.readFile(chatId)
                runOnUiThread {
                    messagesObservable.value = value
                }
            }
        }
    }

    private fun setupChat() {
        dialog = initDialog()
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
                    audioFile = initFile()

                    mediaPlayer.pauseAudio(adapter = adapter)

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

    private fun initFile(): File {
        val audioPath = File("$cacheDir/chats/${speaker}_${roleCode}")
        if (!audioPath.exists()) audioPath.mkdirs()
        return File(audioPath, "user_temp.mp4")
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

                    saveMessages()
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
        initAdapter(false)
        dialog.show()

        getMessages({
            dialog.dismiss()

            messages = it
            compareChats()
        }, {
            dialog.dismiss()
            loadMessages()
        })
    }

    private fun loadMessages() {
        chatMessagesClient.loadMessages(chatId, { loadedMessages ->
            messages = loadedMessages
            generateFirstMessage()

            runOnUiThread {
                initAdapter()
                saveMessages()
            }
        }, {
            if (it != null) showErrorSnack(it, binding.root)

            generateFirstMessage()
            runOnUiThread {
                initAdapter()
                saveMessages()
            }
        })
    }

    private fun compareChats() {
        chatsInfoClient.getMessagesCount(chatId, {
            if (((messages.size - 1) / 2) != it) {
                chatMessagesClient.loadMessages(chatId, { updatedMessages ->
                    messages = updatedMessages
                    generateFirstMessage()

                    runOnUiThread {
                        initAdapter()
                        saveMessages()
                    }
                }, {
                    runOnUiThread {
                        if (it != null) showErrorSnack(it, binding.root)
                    }
                })
            } else {
                generateFirstMessage()
                runOnUiThread {
                    initAdapter()
                    saveMessages()
                }
            }
        }, {
            runOnUiThread {
                val errorMessage = ContextCompat.getString(this@ChatActivity, R.string.unable_to_compare)
                showErrorSnack(errorMessage, binding.root)
            }
        })
    }

    private fun generateFirstMessage() {
        if (messages.isNotEmpty() && messages[0] != viewModel.generateFirstMessage(roleInt)) {
            val firstMessage = viewModel.generateFirstMessage(roleInt)
            messages.add(0, firstMessage)
        } else if (messages.isEmpty()) {
            val firstMessage = viewModel.generateFirstMessage(roleInt)
            messages.add(firstMessage)
        }
    }

    private fun initAdapter(update: Boolean = true) {
        binding.apply {
            adapter = ChatAdapter(messages, this@ChatActivity, chatMessagesClient)
            chatChatRv.layoutManager = LinearLayoutManager(this@ChatActivity)
            chatChatRv.adapter = adapter
            chatChatRv.smoothScrollToPosition(adapter.itemCount)
        }

        if (update) viewModel.updateAdapter(adapter, messages)
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
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setAudioSamplingRate(44100)
            setAudioChannels(1)
            setAudioEncodingBitRate(16*44100)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()

            mediaRecorder = this
        }
    }

    private fun sendMessage(text: String = NULL, audio: String = NULL) {
        chatHelper.showLoadingUtils(binding, this@ChatActivity)
        chatMessagesClient.sendMessage(chatId, text, audio, {
            runOnUiThread {
                messages.add(it)
                viewModel.updateAdapter(adapter, messages)
                chatHelper.hideLoadingUtils(binding)

                if (viewModel.getPlayable()) adapter.playAudio(link = it.audio)
                runOnUiThread {
                    checkIfAppRated()
                    saveMessages()
                }
            }
        }, {
            viewModel.showErrorMessage(it)
        })
    }

    private fun uploadAudio(audioFile: File) {
        chatHelper.showLoadingUtils(binding, this@ChatActivity)
        chatMessagesClient.uploadAudio(audioFile, {
            if (!it.matches(Regex("^<.*"))) {
                runOnUiThread {
                    chatHelper.hideLoadingUtils(binding)
                    messages.add(Message(NULL, USER, it))
                    viewModel.updateAdapter(adapter, messages)

                    sendMessage(audio = it)
                    saveMessages()
                }
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
                InAppReviewer().askUserForReview(this@ChatActivity) {
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

    private fun savePlayable() {
        sharedPreferences.edit().apply {
            putBoolean("playable_${speaker}_${roleCode}", viewModel.getPlayable())
            apply()
        }
    }

    private fun saveMessages() {
        viewModel.launch {
            database.writeToFile(chatId, messages)
        }
    }

    fun shareIntent(text: String) {
        val finalText = "${resources.getText(R.string.share_text_1)} $speaker ${resources.getText(R.string.share_text_2)} ${resources.getText(R.string.app_name)}:\n\n$text"

        ShareCompat.IntentBuilder(this@ChatActivity)
            .setChooserTitle(resources.getText(R.string.share_using))
            .setType("text/plain")
            .setText(finalText)
            .startChooser()
    }

    private fun checkPermissions() : Boolean {
        if (ContextCompat.checkSelfPermission(this@ChatActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ChatActivity, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_CODE)
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
                audioFile = initFile()

                mediaPlayer.pauseAudio(adapter = adapter)

                startRecording(audioFile)
                binding.chatRecordImage.setImageResource(R.drawable.ic_mic_off)
                binding.chatKeyboardButton.isClickable = false
            }
        }
    }

    private fun initDialog(): Dialog {
        val dialog = Dialog(this@ChatActivity)
        val dialogBinding = DialogLoadingBinding.inflate(LayoutInflater.from(this@ChatActivity))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        dialogBinding.apply {
            val viewArray = arrayListOf<ImageView>()
            viewArray.apply {
                add(loadingDot1)
                add(loadingDot2)
                add(loadingDot3)
                add(loadingDot4)
                add(loadingDot5)
                add(loadingDot6)
                add(loadingDot7)
                add(loadingDot8)
            }

            CoroutineScope(Dispatchers.IO).launch {
                for (i in viewArray) {
                    i.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity, R.anim.dots_recognition_animation))
                    delay(250)
                }
            }
        }

        val width = this@ChatActivity.resources.displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = width - (width / 10)
        dialog.window?.attributes = layoutParams

        return dialog
    }

    fun getBinding() = binding
    fun isRecording() = isRecording
    fun getHelper() = chatHelper
    fun getChatViewModel() = viewModel
    fun getMediaPlayer() = mediaPlayer
}