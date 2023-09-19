package com.ssrlab.assistant.ui.chat

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.utils.ChatHelper
import com.ssrlab.assistant.utils.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

const val PERMISSIONS_REQUEST_CODE = 1
const val SAMPLE_RATE = 44100
const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView

    private val viewModel: ChatViewModel by viewModels()
    private var originalScreenHeight = 0

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
    }

    private fun setUpFFTLayout() {
        visualizerView = FFTVisualizerView(this, null)
        val fftLayout = binding.mainWaveLayout
        fftLayout.addView(visualizerView)
    }

    private fun setUpKeyBoardAction() {
        binding.apply {
            mainKeyboardButton.setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                mainChatMsgInput.requestFocus()
                imm.showSoftInput(mainChatMsgInput, InputMethodManager.SHOW_IMPLICIT)
            }

            val keyboardVisibilityListener = ViewTreeObserver.OnGlobalLayoutListener {
                if (originalScreenHeight == 0) originalScreenHeight = mainView.height
                val screenHeight = mainView.height

                if (originalScreenHeight != screenHeight) {
                    mainChatMsgHolder.visibility = View.VISIBLE
                    mainBottomBar.visibility = View.GONE
                    mainRecordButton.visibility = View.GONE
                } else {
                    mainChatMsgHolder.visibility = View.GONE
                    mainBottomBar.visibility = View.VISIBLE
                    mainRecordButton.visibility = View.VISIBLE
                }
            }

            mainView.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityListener)
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