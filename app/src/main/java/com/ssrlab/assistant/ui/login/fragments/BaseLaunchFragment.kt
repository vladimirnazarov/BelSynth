package com.ssrlab.assistant.ui.login.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.R
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.client.AuthClient
import com.ssrlab.assistant.databinding.DialogLoadingBinding
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.utils.helpers.TextHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseLaunchFragment: Fragment() {

    protected lateinit var launchActivity: LaunchActivity
    protected lateinit var mainApp: MainApplication

    protected lateinit var authClient: AuthClient
    protected lateinit var inputHelper: TextHelper

    protected val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchActivity = activity as LaunchActivity
        mainApp = launchActivity.mainApp

        authClient = AuthClient(mainApp.getContext(), mainApp, launchActivity.sharedPreferences)
        inputHelper = TextHelper(mainApp.getContext())
    }

    protected fun generateLoadingDialog(): Dialog {
        val dialog = Dialog(launchActivity)
        val dialogBinding = DialogLoadingBinding.inflate(LayoutInflater.from(launchActivity))
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
                    i.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.dots_recognition_animation))
                    delay(250)
                }
            }
        }

        val width = launchActivity.resources.displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = width - (width / 10)
        dialog.window?.attributes = layoutParams

        return dialog
    }
}