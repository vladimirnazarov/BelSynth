package com.ssrlab.assistant.ui.main.fragments.base

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.client.AuthClient
import com.ssrlab.assistant.databinding.DialogAttentionBinding
import com.ssrlab.assistant.ui.main.ChooseActivity
import com.ssrlab.assistant.utils.helpers.TextHelper
import kotlin.time.times

open class BaseMainFragment: Fragment() {

    lateinit var chooseActivity: ChooseActivity
    lateinit var mainApp: MainApplication

    lateinit var authClient: AuthClient
    lateinit var inputHelper: TextHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chooseActivity = activity as ChooseActivity
        mainApp = chooseActivity.getMainApp()

        authClient = AuthClient(mainApp.getContext())
        inputHelper = TextHelper(mainApp.getContext())
    }

    /**
     * 1. Title
     * 2. Body
     * 3. Yes
     * 4. No
     */
    @Suppress("DEPRECATION")
    fun initDialog(textRes: ArrayList<String>, onAccept: () -> Unit) {
        val displayMetrics = DisplayMetrics()
        chooseActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        val dialog = Dialog(chooseActivity)
        val dialogBinding = DialogAttentionBinding.inflate(LayoutInflater.from(chooseActivity))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        dialogBinding.apply {
            dialogAttTitle.text = textRes[0]
            dialogAttBody.text = textRes[1]

            dialogAttButtonYes.text = textRes[2]
            dialogAttButtonYes.setOnClickListener { onAccept() }

            dialogAttButtonNo.text = textRes[3]
            dialogAttButtonNo.setOnClickListener { dialog.dismiss() }
        }

        val width = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.width = ((0.9 * width).toInt())
        dialog.window?.attributes = layoutParams

        dialog.show()
    }
}