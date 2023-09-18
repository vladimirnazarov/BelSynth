package com.ssrlab.assistant.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.utils.ChatHelper
import com.ssrlab.assistant.utils.FFTVisualizerView
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.*

private const val PERMISSIONS_REQUEST_CODE = 1
private const val SAMPLE_RATE = 44100
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatHelper: ChatHelper

    private lateinit var audioRecord: AudioRecord
    private lateinit var visualizerView: FFTVisualizerView
    private var isRecording = false

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatHelper = ChatHelper()
        setUpToolbar()

        setUpFFTLayout()
        setUpRecordButton()
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

    private fun setUpRecordButton() {
        binding.mainRecordRipple.setOnClickListener {
            if (!isRecording) {
                if (checkPermissions()) {
                    startRecording()
                    binding.mainRecordImage.setImageResource(R.drawable.ic_mic_off)
                }
            } else {
                stopRecording()
                binding.mainRecordImage.setImageResource(R.drawable.ic_mic_on)
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

    @SuppressLint("MissingPermission")
    private fun startRecording() {
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
            val alphaInAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.alpha_in)

            mainWaveLayout.startAnimation(alphaInAnim)
            mainWaveCenter.startAnimation(alphaInAnim)
            mainDurationHolder.startAnimation(alphaInAnim)

            mainWaveLayout.animation.setAnimationListener(object : AnimationListener {
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
                runOnUiThread { binding.mainDurationText.text = chatHelper.convertToTimeMillis(currentTime) }

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

                visualizerView.updateFFTData(fftData)
            }
        }.start()
    }

    private fun stopRecording() {
        if (isRecording) {
            audioRecord.stop()
            audioRecord.release()
            isRecording = false

            binding.apply {
                val alphaOutAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.alpha_out)

                mainWaveLayout.startAnimation(alphaOutAnim)
                mainWaveCenter.startAnimation(alphaOutAnim)
                mainDurationHolder.startAnimation(alphaOutAnim)

                mainWaveLayout.animation.setAnimationListener(object : AnimationListener {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
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
}