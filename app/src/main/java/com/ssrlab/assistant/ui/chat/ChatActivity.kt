package com.ssrlab.assistant.ui.chat

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.R
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.db.objects.BotMessage
import com.ssrlab.assistant.db.objects.MessageInfoObject
import com.ssrlab.assistant.db.objects.UserMessage
import com.ssrlab.assistant.db.objects.UserVoiceMessage
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.utils.PERMISSIONS_REQUEST_CODE
import com.ssrlab.assistant.utils.PREFERENCES
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.TextHelper
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject
import com.ssrlab.assistant.utils.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class ChatActivity : AppCompatActivity() {

    private val mainApp = MainApplication()

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    private var speaker = ""
    private var role = ""
    private var roleCode = ""
    private var roleInt = 0

    private lateinit var imm: InputMethodManager
    private var originalScreenHeight = 0

    private var id = 1
    private lateinit var audioFile: File

    private val messagesI = arrayListOf<MessageInfoObject>()
    private val botMessages = arrayListOf<BotMessage>()
    private val userMessages = arrayListOf<UserMessage>()
    private val userVoiceMessages = arrayListOf<UserVoiceMessage>()

    private lateinit var adapter: ChatAdapter

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        mainApp.setContext(this@ChatActivity)
        mainApp.loadPreferences(sharedPreferences)

        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.playable.value = true
        setUpAudioButton()

        viewModel.playable.value = mainApp.isSoundEnabled()

        getIntentValues()

        chatHelper = ChatHelper()
        setUpToolbar()

        setUpFFTLayout()
        setUpKeyBoardAction()
        setUpRecordButton()

        binding.apply {
            val botMessage = viewModel.generateFirstMessage(id, roleInt, this@ChatActivity)
            botMessages.add(botMessage)
            messagesI.add(MessageInfoObject(id, 1))
            adapter = ChatAdapter(messagesI, botMessages, userMessages, userVoiceMessages, this@ChatActivity)

            chatChatRv.layoutManager = LinearLayoutManager(this@ChatActivity)
            chatChatRv.adapter = adapter
            chatChatRv.smoothScrollToPosition(adapter.itemCount)
        }
    }

    override fun onStart() {
        super.onStart()

        chatHelper.loadRecordAnim(this@ChatActivity, binding)
        setUpMessageSendButton()

        val inputHelper = TextHelper(this@ChatActivity)
        binding.chatRvImm.setOnClickListener { inputHelper.hideKeyboard(binding.root) }
        binding.chatToolbar.setOnClickListener { inputHelper.hideKeyboard(binding.root) }
    }

    override fun onStop() {
        super.onStop()

        MediaPlayerObject.pauseAudio(adapter = adapter)
    }

    override fun onDestroy() {
        super.onDestroy()

        getExternalFilesDir(null)
    }

    private fun setUpAudioButton() {
        viewModel.playable.observe(this@ChatActivity) {
            if (!it) {
                binding.chatToolbarAudio.setImageResource(R.drawable.ic_volume_off)
                MediaPlayerObject.pauseAudio(adapter = adapter)

                mainApp.savePreferences(sharedPreferences, this@ChatActivity, value = false)
            }
            else {
                binding.chatToolbarAudio.setImageResource(R.drawable.ic_volume_on)

                mainApp.savePreferences(sharedPreferences, this@ChatActivity, value = true)
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
        viewModel.sendMessage(text, speaker, role = roleCode, chatActivity = this@ChatActivity)
        chatHelper.showLoadingUtils(binding, this@ChatActivity, scope)
    }

    private fun setUpRecordButton() {
        binding.chatRecordRipple.setOnClickListener {
            if (!viewModel.isRecording()) {
                if (checkPermissions()) {
                    id += 1
                    audioFile = File(getExternalFilesDir(null), "uv_msg_${id}_${speaker}.mp4")

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
                userVoiceMessages.last().audio?.let { it1 -> viewModel.sendMessage(it1, speaker, role = roleCode, chatActivity = this@ChatActivity) }
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
                audioFile = File(getExternalFilesDir(null), "uv_msg_${id}_${speaker}.mp4")

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

            if (speaker == "vasil") {
                chatToolbarTitle.visibility = View.GONE
                chatToolbarTextBlockFull.visibility = View.VISIBLE

                chatToolbarTitleFull.text = intent.getStringExtra("chat_name")
                chatToolbarSubTitleFull.text = role
            } else {
                chatToolbarTitle.text = intent.getStringExtra("chat_name")

                chatToolbarTitle.visibility = View.VISIBLE
                chatToolbarTextBlockFull.visibility = View.GONE
            }
        }
    }

    private fun getIntentValues() {
        speaker = intent.getStringExtra("chat_id").toString()
        role = intent.getStringExtra("chat_role").toString()
        roleCode = intent.getStringExtra("chat_role_code").toString()
        roleInt = intent.getIntExtra("chat_role_int", 0)
        binding.chatToolbarImage.setImageResource(intent.getIntExtra("chat_img", R.drawable.img_speaker_1))
    }
}