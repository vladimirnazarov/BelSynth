package com.ssrlab.assistant.utils.helpers.text

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityChatBinding
import com.ssrlab.assistant.ui.chat.ChatActivity
import kotlinx.coroutines.*

class ChatHelper {

    private fun loadDotsAnim(context: Context, binding: ActivityChatBinding, scope: CoroutineScope) {
        val viewArray = arrayListOf<ImageView>()
        binding.apply {
                viewArray.add(chatProgressDot1)
                viewArray.add(chatProgressDot2)
                viewArray.add(chatProgressDot3)
                viewArray.add(chatProgressDot4)
                viewArray.add(chatProgressDot5)
                viewArray.add(chatProgressDot6)
                viewArray.add(chatProgressDot7)
                viewArray.add(chatProgressDot8)
        }

        scope.launch {
            for (i in viewArray) {
                i.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dots_recognition_animation))
                delay(250)
            }
        }
    }

    fun showLoadingUtils(binding: ActivityChatBinding, chatActivity: ChatActivity) {
        binding.apply {
            chatProgressHolder.visibility = View.VISIBLE

            val scope = CoroutineScope(Dispatchers.IO + Job())
            loadDotsAnim(chatActivity, binding, scope)

            chatRecordRipple.isClickable = false
            chatKeyboardButton.isClickable = false
        }
    }

    fun hideLoadingUtils(binding: ActivityChatBinding) {
        binding.apply {
            chatProgressHolder.visibility = View.GONE

            chatRecordRipple.isClickable = true
            chatKeyboardButton.isClickable = true
        }
    }

    fun convertToTimerMode(duration: Int) : String {
        val minute = duration % (1000 * 60 * 60) / (1000 * 60)
        val seconds = duration % (1000 * 60 * 60) % (1000 * 60) / 1000

        var finalString = ""
        finalString += "0$minute:"
        if (seconds < 10) finalString += "0"
        finalString += "$seconds"

        return finalString
    }
}