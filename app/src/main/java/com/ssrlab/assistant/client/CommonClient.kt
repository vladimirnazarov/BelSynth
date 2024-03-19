package com.ssrlab.assistant.client

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.DialogLoadingBinding
import com.ssrlab.assistant.utils.REQUEST_TIME_OUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

open class CommonClient {

    var client = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .build()

    private val job = Job()
    val scope = CoroutineScope(Dispatchers.IO + job)

    val fireAuth = FirebaseAuth.getInstance()

    fun initDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        val dialogBinding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        dialogBinding.apply {
            val viewArray = arrayListOf<ImageView>()
            viewArray.apply {
                add(loadingDot1)
                add(loadingDot2)
                add(loadingDot3)
                add(loadingDot4)
                add(loadingDot5)
                add(loadingDot6)
                add(loadingDot7)
                add(loadingDot8)
            }

            scope.launch {
                for (i in viewArray) {
                    i.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dots_recognition_animation))
                    delay(250)
                }
            }
        }

        val width = context.resources.displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = width - (width / 10)
        dialog.window?.attributes = layoutParams

        return dialog
    }
}