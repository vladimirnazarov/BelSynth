package com.ssrlab.assistant.utils

import android.content.Context
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class ChatAnimHelper {

    fun loadDotsAnim(context: Context, binding: ActivityMainBinding, scope: CoroutineScope) {
        val viewArray = arrayListOf<ImageView>()
        binding.apply {
                viewArray.add(mainProgressDot1)
                viewArray.add(mainProgressDot2)
                viewArray.add(mainProgressDot3)
                viewArray.add(mainProgressDot4)
                viewArray.add(mainProgressDot5)
                viewArray.add(mainProgressDot6)
                viewArray.add(mainProgressDot7)
                viewArray.add(mainProgressDot8)
        }

        scope.launch {
            for (i in viewArray) {
                i.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recognition_dots_animation))
                delay(250)
            }
        }
    }

    fun loadRecordAnim(context: Context, binding: ActivityMainBinding) {
        binding.mainDurationIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.record_animation))
    }
}