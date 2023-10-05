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
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.R
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.db.objects.BotMessage
import com.ssrlab.assistant.db.objects.MessageInfoObject
import com.ssrlab.assistant.db.objects.UserMessage
import com.ssrlab.assistant.db.objects.UserVoiceMessage
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.utils.*
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject
import com.ssrlab.assistant.utils.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModel
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val mainApp = MainApplication()

    private lateinit var binding: ActivityChatBinding
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

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp.setContext(this@ChatActivity)

        viewModel.playable.value = true
        setUpAudioButton()

        loadPreferences()

        speaker = intent.getStringExtra("chat_id").toString()
        binding.chatToolbarImage.setImageResource(intent.getIntExtra("chat_img", R.drawable.img_speaker_1))

        chatHelper = ChatHelper()
        setUpToolbar()

        setUpFFTLayout()
        setUpKeyBoardAction()
        setUpRecordButton()

        binding.apply {
            adapter = ChatAdapter(messagesI, botMessages, userMessages, userVoiceMessages, this@ChatActivity)

            chatChatRv.layoutManager = LinearLayoutManager(this@ChatActivity)
            chatChatRv.adapter = adapter
            chatChatRv.smoothScrollToPosition(adapter.itemCount)
        }
    }

    override fun onResume() {
        super.onResume()

        chatHelper.loadRecordAnim(this@ChatActivity, binding)
        setUpMessageSendButton()
    }

    override fun onStop() {
        super.onStop()

        MediaPlayerObject.pauseAudio(adapter = adapter)
    }

    override fun onDestroy() {
        super.onDestroy()

        getExternalFilesDir(null)
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val locale = sharedPreferences.getString(LOCALE, "be")
        val isSoundEnabled = sharedPreferences.getBoolean(CHAT_SOUND, true)

        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }
        viewModel.playable.value = isSoundEnabled

        val config = mainApp.getContext().resources.configuration
        config.setLocale(locale?.let { Locale(it) })
        locale?.let { Locale(it) }?.let { Locale.setDefault(it) }

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)
        mainApp.setLocale(locale!!)
    }

    private fun savePreferences(value: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean(CHAT_SOUND, value)
            apply()
        }
    }

    private fun setUpAudioButton() {
        viewModel.playable.observe(this@ChatActivity) {
            if (!it) {
                binding.chatToolbarAudio.setImageResource(R.drawable.ic_volume_off)
                MediaPlayerObject.pauseAudio(adapter = adapter)

                savePreferences(false)
            }
            else {
                binding.chatToolbarAudio.setImageResource(R.drawable.ic_volume_on)

                savePreferences(true)
            }
        }

        binding.chatToolbarAudio.setOnClickListener {
            viewModel.playable.value = !viewModel.playable.value!!
        }
    }

    private fun setUpFFTLayout() {
        visualizerView = FFTVisualizerView(this, null)
        val fftLayout = binding.chatWaveLayout
        fftLayout.addView(visualizerView)
    }

    private fun setUpKeyBoardAction() {
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
                    viewModel.controlBottomVisibility(this@ChatActivity)
                    chatChatRv.smoothScrollToPosition(adapter.itemCount - 1)
                }
                else {
                    viewModel.apply {
                        controlBottomVisibility( this@ChatActivity, false)
                    }
                }
            }

            chatView.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityListener)
        }
    }

    private fun setUpMessageSendButton() {
        binding.apply {
            chatChatMsgSend.setOnClickListener {
                if (chatChatMsgInput.text?.isNotEmpty() == true) {
                    loadUserMessage(chatChatMsgInput.text!!.toString())

                    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }

                    chatChatMsgInput.text?.toString()?.replace("\n", ". ")?.let { it1 -> sendTextMessage(it1) }
                    chatChatMsgInput.text?.clear()
                }
            }
        }
    }

    private fun sendTextMessage(text: String) {
        viewModel.sendMessage(text, speaker, chatActivity = this@ChatActivity)
        chatHelper.showLoadingUtils(binding, this@ChatActivity, scope)
    }

    private fun setUpRecordButton() {
        binding.chatRecordRipple.setOnClickListener {
            if (!viewModel.isRecording()) {
                if (checkPermissions()) {
                    id += 1
                    audioFile = File(getExternalFilesDir(null), "uv_msg_${id}_${speaker}.mp3")

                    MediaPlayerObject.pauseAudio(adapter = adapter)

                    viewModel.startRecording(this@ChatActivity, audioFile)
                    binding.chatRecordImage.setImageResource(R.drawable.ic_mic_off)
                    binding.chatKeyboardButton.isClickable = false
                }
            } else {
                viewModel.stopRecording(binding)

                binding.chatRecordImage.setImageResource(R.drawable.ic_mic_on)
                binding.chatKeyboardButton.isClickable = true

                loadUserVoiceMessage(audioFile)
                userVoiceMessages.last().audio?.let { it1 -> viewModel.sendMessage(it1, speaker, chatActivity = this@ChatActivity) }
                chatHelper.showLoadingUtils(binding, this@ChatActivity, scope)
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
            binding.chatChatRv.smoothScrollToPosition(adapter.itemCount)
            delay(200)
            binding.chatChatRv.smoothScrollToPosition(adapter.itemCount)
        }
    }

    private fun goBack() { onBackPressedDispatcher.onBackPressed() }

    fun shareIntent(text: String) {
        val finalText = "${resources.getText(R.string.share_text_1)} $speaker ${resources.getText(R.string.share_text_2)} ${resources.getText(R.string.app_name)}:\n\n$text"

        ShareCompat.IntentBuilder(this@ChatActivity)
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
                id += 1
                audioFile = File(getExternalFilesDir(null), "uv_msg_${id}_${speaker}.mp3")

                MediaPlayerObject.pauseAudio(adapter = adapter)

                viewModel.startRecording(this@ChatActivity, audioFile)
                binding.chatRecordImage.setImageResource(R.drawable.ic_mic_off)
                binding.chatKeyboardButton.isClickable = false
            }
        }
    }

    private fun setUpToolbar() {
        binding.apply {
            chatToolbarBack.setOnClickListener { goBack() }
            chatToolbarTitle.text = intent.getStringExtra("chat_name")
        }
    }
}