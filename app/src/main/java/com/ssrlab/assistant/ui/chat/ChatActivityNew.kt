package com.ssrlab.assistant.ui.chat

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.utils.*
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject
import com.ssrlab.assistant.utils.view.FFTVisualizerView
import com.ssrlab.assistant.utils.vm.ChatViewModelNew
import java.io.File

class ChatActivityNew: BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatHelper: ChatHelper
    private lateinit var visualizerView: FFTVisualizerView

    private val viewModel: ChatViewModelNew by viewModels {
        ChatViewModelNew.Factory(this@ChatActivityNew)
    }

    private var playableValue = true
    private var speaker = ""
    private var role = ""
    private var roleCode = ""
    private var roleInt = 0

    private var id = 1
    private lateinit var audioFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        mainApp.setContext(this@ChatActivityNew)
        mainApp.loadPreferences(sharedPreferences)

        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStop() {
        super.onStop()

        saveData()
    }

    private fun setupChat() {
        getIntents()

        playableValue = sharedPreferences.getBoolean("playable_${speaker}_${role}", true)
    }

    private fun setupAudioButton() {
        viewModel.playable.observe(this@ChatActivityNew) {
            if (!it) {
                binding.chatToolbarAudio.setImageResource(R.drawable.ic_volume_off)
                MediaPlayerObject.pauseAudio(adapter = adapter)
            } else {
                binding.chatToolbarAudio.setImageResource(R.drawable.ic_volume_on)
            }
        }

        binding.chatToolbarAudio.setOnClickListener {
            viewModel.playable.value = !viewModel.playable.value!!
        }
    }

    private fun setupToolbar() {
        binding.chatToolbarBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (speaker == BOT_6) setupToolbarByType(1)
        else setupToolbarByType(0)
    }

    /**
     * 0 - Common
     * 1 - Role
     */
    private fun setupToolbarByType(type: Int) {
        binding.apply {
            when (type) {
                0 -> {
                    chatToolbarTitle.text = intent.getStringExtra(CHAT_NAME)

                    chatToolbarTitle.visibility = View.VISIBLE
                    chatToolbarTextBlockFull.visibility = View.GONE
                }

                1 -> {
                    chatToolbarTitleFull.text = intent.getStringExtra(CHAT_NAME)
                    chatToolbarSubTitleFull.text = role

                    chatToolbarTitle.visibility = View.GONE
                    chatToolbarTextBlockFull.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getIntents() {
        speaker = intent.getStringExtra(CHAT_ID).toString()
        role = intent.getStringExtra(CHAT_ROLE).toString()
        roleCode = intent.getStringExtra(CHAT_ROLE_CODE).toString()
        roleInt = intent.getIntExtra(CHAT_ROLE_INT, 0)
        binding.chatToolbarImage.setImageResource(intent.getIntExtra(CHAT_IMAGE, R.drawable.img_speaker_1))
    }

    private fun saveData() {
        sharedPreferences.edit().apply {
            putBoolean("playable_${speaker}_${role}", playableValue)
            apply()
        }
    }

    fun getChatHelper() = chatHelper
    fun getVisualizerView() = visualizerView
    fun getId(): Int {
        id++
        return id - 1
    }
    fun getSpeaker() = speaker
    fun getBinding() = binding
}