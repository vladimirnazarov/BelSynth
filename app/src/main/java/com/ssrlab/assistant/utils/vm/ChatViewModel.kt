package com.ssrlab.assistant.utils.vm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.ui.chat.*
import com.ssrlab.assistant.utils.AUDIO_FORMAT
import com.ssrlab.assistant.utils.CHANNEL_CONFIG
import com.ssrlab.assistant.utils.PERMISSIONS_REQUEST_CODE
import com.ssrlab.assistant.utils.SAMPLE_RATE
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.*

class ChatViewModel : ViewModel() {

    private lateinit var audioRecord: AudioRecord
    private var isRecording = false

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun setUpRecordButton(binding: ActivityMainBinding, mainActivity: MainActivity) {
        binding.mainRecordRipple.setOnClickListener {
            if (!isRecording) {
                if (checkPermissions(mainActivity)) {
                    startRecording(binding, mainActivity)
                    binding.mainRecordImage.setImageResource(R.drawable.ic_mic_off)
                    binding.mainKeyboardButton.isClickable = false
                }
            } else {
                stopRecording(binding, mainActivity)
                binding.mainRecordImage.setImageResource(R.drawable.ic_mic_on)
                binding.mainKeyboardButton.isClickable = true
            }
        }
    }

    fun startRecording(binding: ActivityMainBinding, mainActivity: MainActivity) {
        startRecordingFunctionality(binding, mainActivity)
    }

    fun controlBottomVisibility(mainActivity: MainActivity, binding: ActivityMainBinding, hideBottom: Boolean = true) {
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

            mainWaveLayout.animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    mainWaveLayout.visibility = View.VISIBLE
                    mainWaveCenter.visibility = View.VISIBLE
                    mainDurationHolder.visibility = View.VISIBLE
                    mainWaveReplacement.visibility = View.GONE
                }
                override fun onAnimationRepeat(p0: Animation?) {}
            })
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

    private fun stopRecording(binding: ActivityMainBinding, mainActivity: MainActivity) {
        if (isRecording) {
            audioRecord.stop()
            audioRecord.release()
            isRecording = false

            binding.apply {
                val alphaOutAnim = AnimationUtils.loadAnimation(mainActivity, R.anim.alpha_out)

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

    private fun checkPermissions(mainActivity: MainActivity) : Boolean {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_CODE)
            return false
        }
        return true
    }
}