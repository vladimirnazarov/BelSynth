package com.ssrlab.assistant.ui.chat

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.db.ChatMessage
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.utils.PERMISSIONS_REQUEST_CODE
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var imm: InputMethodManager
    private var id = 0
    private var originalScreenHeight = 0

    private val messages = arrayListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatHelper = ChatHelper()
        setUpToolbar()

        setUpFFTLayout()
        setUpKeyBoardAction()
        viewModel.setUpRecordButton(binding, this@MainActivity)
    }

    override fun onResume() {
        super.onResume()

        chatHelper.apply {
            loadDotsAnim(this@MainActivity, binding, scope)
            loadRecordAnim(this@MainActivity, binding)
        }

        binding.apply {
            messages.add(ChatMessage(id, resources.getString(R.string.chat_greeting)))
            messages.add(ChatMessage(id, "test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test "))
            messages.add(ChatMessage(id, "test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test "))
            messages.add(ChatMessage(id, audio = true))
            messages.add(ChatMessage(id, "test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test "))
            messages.add(ChatMessage(id, audio = true))
            messages.add(ChatMessage(id, "test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test "))
            messages.add(ChatMessage(id, "test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test "))

            adapter = ChatAdapter(messages)

            val layoutManager = LinearLayoutManager(this@MainActivity)
            layoutManager.reverseLayout = true

            mainChatRv.layoutManager = LinearLayoutManager(this@MainActivity)
            mainChatRv.adapter = adapter
            mainChatRv.scrollToPosition(messages.size - 1)
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
                    mainChatRv.smoothScrollToPosition(adapter.itemCount)
                }
                else {
                    viewModel.controlBottomVisibility( this@MainActivity, binding, false)
                    mainChatRv.smoothScrollToPosition(adapter.itemCount)
                }
            }

            mainView.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityListener)
        }
    }

    private fun setUpMessageSendButton() {
        binding.apply {
            mainChatMsgSend.setOnClickListener {
                if (mainChatMsgInput.text?.isNotEmpty() == true) {
                    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
                    messages.add(ChatMessage(id, mainChatMsgInput.text?.toString()!!))
                    adapter.notifyItemInserted(messages.size - 1)

                    mainChatRv.adapter = adapter
                    mainChatMsgInput.text?.clear()
                }
            }
        }
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

    fun getChatHelper() = chatHelper
    fun getVisualizerView() = visualizerView
}