package com.ssrlab.assistant.ui.choose.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.client.AuthClient
import com.ssrlab.assistant.databinding.DialogAttentionBinding
import com.ssrlab.assistant.ui.choose.ChooseActivity
import com.ssrlab.assistant.utils.helpers.TextHelper

open class BaseChooseFragment: Fragment() {

    lateinit var chooseActivity: ChooseActivity
    lateinit var mainApp: MainApplication

    lateinit var authClient: AuthClient
    lateinit var inputHelper: TextHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chooseActivity = activity as ChooseActivity
        mainApp = chooseActivity.mainApp

        authClient = AuthClient(mainApp.getContext(), mainApp, chooseActivity.sharedPreferences)
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
            dialogAttButtonYes.setOnClickListener {
                dialog.dismiss()
                onAccept()
            }

            dialogAttButtonNo.text = textRes[3]
            dialogAttButtonNo.setOnClickListener { dialog.dismiss() }
        }

        val width = chooseActivity.resources.displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = width - (width / 10)
        dialog.window?.attributes = layoutParams

        dialog.show()
    }
}