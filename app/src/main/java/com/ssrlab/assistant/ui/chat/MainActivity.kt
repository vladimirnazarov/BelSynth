package com.ssrlab.assistant.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.db.objects.BotMessage
import com.ssrlab.assistant.db.objects.MessageInfoObject
import com.ssrlab.assistant.db.objects.UserMessage
import com.ssrlab.assistant.db.objects.UserVoiceMessage
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.utils.PERMISSIONS_REQUEST_CODE
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject
import com.ssrlab.assistant.utils.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModel
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView

    private val viewModel: ChatViewModel by viewModels()
    private var speaker = ""

    private lateinit var imm: InputMethodManager
    private var originalScreenHeight = 0

    private var id = 1
    private lateinit var audioFile: File

    private val messagesI = arrayListOf(MessageInfoObject(id, 1))
    private val botMessages = arrayListOf(BotMessage(id, text = "Вітаю! Чым я магу дапамагчы?"))
    private val userMessages = arrayListOf<UserMessage>()
    private val userVoiceMessages = arrayListOf<UserVoiceMessage>()

    private lateinit var adapter: ChatAdapter

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.playable.value = true
        setUpAudioButton()

        speaker = intent.getStringExtra("chat_id").toString()

        chatHelper = ChatHelper()
        setUpToolbar()

        setUpFFTLayout()
        setUpKeyBoardAction()
        setUpRecordButton()

        binding.apply {
            adapter = ChatAdapter(messagesI, botMessages, userMessages, userVoiceMessages, this@MainActivity)

            mainChatRv.layoutManager = LinearLayoutManager(this@MainActivity)
            mainChatRv.adapter = adapter
            mainChatRv.smoothScrollToPosition(adapter.itemCount)
        }
    }

    override fun onResume() {
        super.onResume()

        chatHelper.loadRecordAnim(this@MainActivity, binding)
        setUpMessageSendButton()
    }

    override fun onStop() {
        super.onStop()

        MediaPlayerObject.pauseAudio()
    }

    private fun setUpAudioButton() {
        viewModel.playable.observe(this@MainActivity) {
            if (!it) {
                binding.mainToolbarAudio.setImageResource(R.drawable.ic_volume_off)
                MediaPlayerObject.pauseAudio()
            }
            else binding.mainToolbarAudio.setImageResource(R.drawable.ic_volume_on)
        }

        binding.mainToolbarAudio.setOnClickListener {
            viewModel.playable.value = !viewModel.playable.value!!
        }
    }

    private fun setUpFFTLayout() {
        visualizerView = FFTVisualizerView(this, null)
        val fftLayout = binding.mainWaveLayout
        fftLayout.addView(visualizerView)
    }

    private fun setUpKeyBoardAction() {
        binding.apply {
            mainKeyboardButton.setOnClickListener {
                imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                mainChatMsgInput.requestFocus()
                imm.showSoftInput(mainChatMsgInput, InputMethodManager.SHOW_IMPLICIT)
            }

            val keyboardVisibilityListener = ViewTreeObserver.OnGlobalLayoutListener {
                if (originalScreenHeight == 0) originalScreenHeight = mainView.height
                val screenHeight = mainView.height

                if (originalScreenHeight != screenHeight) {
                    viewModel.controlBottomVisibility(this@MainActivity)
                    mainChatRv.smoothScrollToPosition(adapter.itemCount - 1)
                }
                else {
                    viewModel.apply {
                        controlBottomVisibility( this@MainActivity, false)
                    }
                }
            }

            mainView.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityListener)
        }
    }

    private fun setUpMessageSendButton() {
        binding.apply {
            mainChatMsgSend.setOnClickListener {
                if (mainChatMsgInput.text?.isNotEmpty() == true) {
                    loadUserMessage(mainChatMsgInput.text!!.toString())

                    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
                    sendTextMessage(mainChatMsgInput.text!!.toString())

                    mainChatMsgInput.text?.clear()
                }
            }
        }
    }

    private fun sendTextMessage(text: String) {
        viewModel.sendMessage(text, speaker, mainActivity = this@MainActivity)
        chatHelper.showLoadingUtils(binding, this@MainActivity, scope)
    }

    private fun setUpRecordButton() {
        binding.mainRecordRipple.setOnClickListener {
            if (!viewModel.isRecording()) {
                if (checkPermissions()) {
                    id += 1
                    audioFile = File(getExternalFilesDir(null), "uv_msg_${id}_${speaker}.mp3")

                    MediaPlayerObject.pauseAudio()

                    viewModel.startRecording(this@MainActivity, audioFile)
                    binding.mainRecordImage.setImageResource(R.drawable.ic_mic_off)
                    binding.mainKeyboardButton.isClickable = false
                }
            } else {
                viewModel.stopRecording(binding)

                binding.mainRecordImage.setImageResource(R.drawable.ic_mic_on)
                binding.mainKeyboardButton.isClickable = true

                loadUserVoiceMessage(audioFile)
                userVoiceMessages.last().audio?.let { it1 -> viewModel.sendMessage(it1, speaker, mainActivity = this@MainActivity) }
                chatHelper.showLoadingUtils(binding, this@MainActivity, scope)
            }
        }
    }

    fun loadBotMessage(text: String, audioFile: File) {
        botMessages.add(BotMessage(id, text, audioFile))
        messagesI.add(MessageInfoObject(id, 1))

        updateAdapter()
    }

    private fun loadUserMessage(text: String) {
        id += 1
        userMessages.add(UserMessage(id, text))
        messagesI.add(MessageInfoObject(id, 2))

        updateAdapter()
    }

    private fun loadUserVoiceMessage(audio: File? = null) {
        userVoiceMessages.add(UserVoiceMessage(id, audio))
        messagesI.add(MessageInfoObject(id, 3))

        updateAdapter()
    }

    private fun updateAdapter() {
        adapter.notifyItemInserted(messagesI.size - 1)

        scope.launch {
            delay(200)
            binding.mainChatRv.smoothScrollToPosition(adapter.itemCount)
            delay(200)
            binding.mainChatRv.smoothScrollToPosition(adapter.itemCount)
        }
    }

    private fun goBack() { onBackPressedDispatcher.onBackPressed() }

    fun shareIntent(text: String) {
        val finalText = "${resources.getText(R.string.share_text_1)} $speaker ${resources.getText(R.string.share_text_2)} ${resources.getText(R.string.app_name)}:\n$text"

        ShareCompat.IntentBuilder(this@MainActivity)
            .setChooserTitle(resources.getText(R.string.share_using))
            .setType("text/plain")
            .setText(finalText)
            .startChooser()
    }

    fun getChatHelper() = chatHelper
    fun getVisualizerView() = visualizerView
    fun getId(): Int {
        id += 1
        return id - 1
    }
    fun getSpeaker() = speaker
    fun getBinding() = binding

    private fun checkPermissions() : Boolean {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_CODE)
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
                id += 1
                audioFile = File(getExternalFilesDir(null), "uv_msg_${id}_${speaker}.mp3")

                MediaPlayerObject.pauseAudio()

                viewModel.startRecording(this@MainActivity, audioFile)
                binding.mainRecordImage.setImageResource(R.drawable.ic_mic_off)
                binding.mainKeyboardButton.isClickable = false
            }
        }
    }

    private fun setUpToolbar() {
        binding.apply {
            mainToolbarBack.setOnClickListener { goBack() }
            mainToolbarTitle.text = intent.getStringExtra("chat_name")
        }
    }
}