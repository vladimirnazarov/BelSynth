package com.ssrlab.assistant.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.db.*
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.utils.PERMISSIONS_REQUEST_CODE
import com.ssrlab.assistant.utils.helpers.ChatHelper
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

        speaker = intent.getStringExtra("chat_id").toString()

        chatHelper = ChatHelper()
        setUpToolbar()

        setUpFFTLayout()
        setUpKeyBoardAction()
        setUpRecordButton()
    }

    override fun onResume() {
        super.onResume()

        chatHelper.loadRecordAnim(this@MainActivity, binding)

        binding.apply {
            adapter = ChatAdapter(messagesI, botMessages, userMessages, userVoiceMessages)

            mainChatRv.layoutManager = LinearLayoutManager(this@MainActivity)
            mainChatRv.adapter = adapter
            mainChatRv.smoothScrollToPosition(adapter.itemCount)
        }

        setUpMessageSendButton()
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
                    viewModel.controlBottomVisibility(this@MainActivity, binding)
                    mainChatRv.smoothScrollToPosition(adapter.itemCount - 1)
                }
                else {
                    viewModel.apply {
                        controlBottomVisibility( this@MainActivity, binding, false)
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
                    val message = ChatMessage(text = mainChatMsgInput.text?.toString()!!)
                    loadUserMessage(mainChatMsgInput.text!!.toString())

                    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
                    mainChatMsgInput.text?.clear()

                    sendTextMessage(message)
                }
            }
        }
    }

    private fun sendTextMessage(message: ChatMessage) {
        viewModel.sendMessage(text = message.text, speaker = speaker, mainActivity = this@MainActivity)
        showLoadingUtils()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.startRecording(binding, this@MainActivity)
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

    private fun goBack() { onBackPressedDispatcher.onBackPressed() }

    fun loadBotMessage(text: String, audioLink: String) {
        id += 1
        botMessages.add(BotMessage(id, text, audioLink))
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
        id += 1
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

    fun hideLoadingUtils() {
        binding.apply {
            mainProgressHolder.visibility = View.GONE

            mainRecordButton.isClickable = true
            mainKeyboardButton.isClickable = true
        }
    }

    private fun setUpRecordButton() {
        binding.mainRecordRipple.setOnClickListener {
            if (!viewModel.isRecording()) {
                if (checkPermissions()) {
                    viewModel.startRecording(binding, this@MainActivity)
                    binding.mainRecordImage.setImageResource(R.drawable.ic_mic_off)
                    binding.mainKeyboardButton.isClickable = false
                }
            } else {
                stopRecording()

                binding.mainRecordImage.setImageResource(R.drawable.ic_mic_on)
                binding.mainKeyboardButton.isClickable = true

                loadUserVoiceMessage()
                viewModel.sendMessage(audio = userVoiceMessages.last().audio, speaker = speaker, mainActivity = this@MainActivity)
                showLoadingUtils()
            }
        }
    }

    private fun stopRecording() {
        if (viewModel.isRecording()) {
            viewModel.getAudioRecord().stop()
            viewModel.getAudioRecord().release()
            viewModel.setIsRecording(false)

            binding.apply {
                val alphaOutAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.alpha_out)

                mainWaveLayout.startAnimation(alphaOutAnim)
                mainWaveCenter.startAnimation(alphaOutAnim)
                mainDurationHolder.startAnimation(alphaOutAnim)

                mainWaveLayout.animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
                        mainWaveLayout.visibility = View.GONE
                        mainWaveCenter.visibility = View.GONE
                        mainDurationHolder.visibility = View.GONE
                        mainWaveReplacement.visibility = View.VISIBLE
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                })
            }
        }
    }

    private fun checkPermissions() : Boolean {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_CODE)
            return false
        }
        return true
    }

    private fun showLoadingUtils() {
        binding.apply {
            mainProgressHolder.visibility = View.VISIBLE
            chatHelper.loadDotsAnim(this@MainActivity, binding, scope)

            mainRecordButton.isClickable = false
            mainKeyboardButton.isClickable = false
        }
    }

    fun getChatHelper() = chatHelper
    fun getVisualizerView() = visualizerView
}