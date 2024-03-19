package com.ssrlab.assistant.utils.vm

import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssrlab.assistant.R
import com.ssrlab.assistant.db.objects.Message
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.utils.BOT
import com.ssrlab.assistant.utils.BOT_6
import com.ssrlab.assistant.utils.helpers.text.ChatHelper
import com.ssrlab.assistant.utils.helpers.other.MediaPlayerObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("StaticFieldLeak")
class ChatViewModel(private val chatActivity: ChatActivity) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val chatActivity: ChatActivity
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(chatActivity) as T
        }
    }

    private var playable = MutableLiveData<Boolean>()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun generateFirstMessage(roleInt: Int) : Message {
        val message = Message(text = "Вітаю! Чым я магу дапамагчы?", role = BOT)
        when (roleInt) {
            1 -> { message.text = chatActivity.resources.getString(R.string.role_additional_1) }
            2 -> { message.text = chatActivity.resources.getString(R.string.role_additional_2) }
            3 -> { message.text = chatActivity.resources.getString(R.string.role_additional_3) }
            4 -> { message.text = chatActivity.resources.getString(R.string.role_additional_4) }
            5 -> { message.text = chatActivity.resources.getString(R.string.role_additional_5) }
            6 -> { message.text = chatActivity.resources.getString(R.string.role_additional_6) }
            7 -> { message.text = chatActivity.resources.getString(R.string.role_additional_7) }
            8 -> { message.text = chatActivity.resources.getString(R.string.role_additional_8) }
            9 -> { message.text = chatActivity.resources.getString(R.string.role_additional_9) }
            10 -> { message.text = chatActivity.resources.getString(R.string.role_additional_10) }
            11 -> { message.text = chatActivity.resources.getString(R.string.role_additional_11) }
            12 -> { message.text = chatActivity.resources.getString(R.string.role_additional_12) }
            13 -> { message.text = chatActivity.resources.getString(R.string.role_additional_13) }
        }

        return message
    }

    fun setupToolbar(name: String, role: String, speaker: String) {
        chatActivity.getBinding().chatToolbarBack.setOnClickListener {
            chatActivity.onBackPressedDispatcher.onBackPressed()
        }

        if (speaker == BOT_6) setupToolbarByType(1, name, role)
        else setupToolbarByType(0, name, role)
    }

    /**
     * 0 - Common
     * 1 - Role
     */
    private fun setupToolbarByType(type: Int, name: String, role: String) {
        chatActivity.getBinding().apply {
            when (type) {
                0 -> {
                    chatToolbarTitle.text = name

                    chatToolbarTitle.visibility = View.VISIBLE
                    chatToolbarTextBlockFull.visibility = View.GONE
                }

                1 -> {
                    chatToolbarTitleFull.text = name
                    chatToolbarSubTitleFull.text = role

                    chatToolbarTitle.visibility = View.GONE
                    chatToolbarTextBlockFull.visibility = View.VISIBLE
                }
            }
        }
    }

    fun setupAudioButton(adapter: ChatAdapter) {
        playable.observe(chatActivity) {
            if (!it) {
                chatActivity.getBinding().chatToolbarAudio.setImageResource(R.drawable.ic_volume_off)
                MediaPlayerObject.pauseAudio(adapter = adapter)
            } else {
                chatActivity.getBinding().chatToolbarAudio.setImageResource(R.drawable.ic_volume_on)
            }
        }

        chatActivity.getBinding().chatToolbarAudio.setOnClickListener {
            playable.value = !playable.value!!
        }
    }

    fun updateAdapter(adapter: ChatAdapter, messages: ArrayList<Message>) {
        adapter.notifyItemInserted(messages.size)

        scope.launch {
            chatScrollDown(adapter)
        }
    }

    private suspend fun chatScrollDown(adapter: ChatAdapter) {
        delay(200)
        chatActivity.getBinding().chatChatRv.smoothScrollToPosition(adapter.itemCount)
    }

    fun startTimer() {
        chatActivity.getBinding().apply {
            val alphaInAnim = AnimationUtils.loadAnimation(chatActivity, R.anim.alpha_in)
            chatDurationHolder.startAnimation(alphaInAnim)
            chatDurationHolder.visibility = View.VISIBLE
        }

        launch {
            var currentTime = 0

            chatActivity.apply {
                while (isRecording()) {

                    runOnUiThread { getBinding().chatDurationText.text = getHelper().convertToTimerMode(currentTime) }

                    delay(1000)
                    currentTime += 1000
                }
            }
        }
    }

    fun hideTimerAndWave() {
        chatActivity.getBinding().apply {
            chatWaveLayout.visibility = View.GONE
            chatWaveCenter.visibility = View.GONE
            chatDurationHolder.visibility = View.GONE
            chatWaveReplacement.visibility = View.VISIBLE
        }
    }

    fun showErrorMessage(msg: String) {
        chatActivity.apply {
            runOnUiThread {
                showErrorSnack("$msg, try again", chatActivity.getBinding().root)
                ChatHelper().hideLoadingUtils(chatActivity.getBinding())
            }
        }
    }

    fun controlBottomVisibility(hideBottom: Boolean = true) {
        if (!chatActivity.isRecording()) {
            if (hideBottom) {
                chatActivity.getBinding().apply {
                    chatActivity.runOnUiThread {
                        chatChatMsgHolder.visibility = View.VISIBLE
                        chatBottomBar.visibility = View.GONE
                        chatRecordButton.visibility = View.GONE
                    }
                }
            } else {
                scope.launch {
                    delay(50)

                    chatActivity.runOnUiThread {
                        chatActivity.getBinding().apply {
                            chatChatMsgHolder.visibility = View.GONE
                            chatBottomBar.visibility = View.VISIBLE
                            chatRecordButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    fun showWave() {
        chatActivity.getBinding().apply {
            val alphaInAnim = AnimationUtils.loadAnimation(chatActivity, R.anim.alpha_in)

            chatWaveLayout.startAnimation(alphaInAnim)
            chatWaveCenter.startAnimation(alphaInAnim)

            chatWaveLayout.visibility = View.VISIBLE
            chatWaveCenter.visibility = View.VISIBLE
            chatWaveReplacement.visibility = View.GONE
        }
    }

    fun launch(onAction: suspend () -> Unit) {
        scope.launch { onAction() }
    }

    fun setPlayable(value: Boolean) {
        playable.value = value
    }

    fun getPlayable() = playable.value!!
}