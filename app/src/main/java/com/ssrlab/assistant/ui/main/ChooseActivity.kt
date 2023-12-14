package com.ssrlab.assistant.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityChooseBinding
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.utils.PREFERENCES
import com.ssrlab.assistant.utils.THEME
import com.ssrlab.assistant.utils.helpers.LaunchToolbarAnimHelper

class ChooseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseBinding
    private lateinit var animHelper: LaunchToolbarAnimHelper

    private val mainApp = MainApplication()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        mainApp.setContext(this@ChooseActivity)
        mainApp.loadPreferences(sharedPreferences)

        super.onCreate(savedInstanceState)

        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animHelper = LaunchToolbarAnimHelper()
    }

    fun saveTheme(theme: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(THEME, theme)
            apply()
        }

        recreate()
    }

    fun setUpToolbar(title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        animHelper.setUpToolbar(this@ChooseActivity, binding, title, isBackButtonVisible, isAdditionalButtonsVisible, navController)
    }

    fun intentToChat(chatId: String, title: String, img: Int, roleCode: String = "assistant", roleInt: Int = 0, role: String = "") {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chat_id", chatId)
        intent.putExtra("chat_name", title)
        intent.putExtra("chat_img", img)
        intent.putExtra("chat_role_code", roleCode)
        intent.putExtra("chat_role_int", roleInt)
        intent.putExtra("chat_role", role)
        startActivity(intent)
    }

    fun intentToPhone(number: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    fun intentToMail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:hello@asistent.by")
        startActivity(intent)
    }

    fun getMainApp() = mainApp
    fun getSharedPreferences() = sharedPreferences
}