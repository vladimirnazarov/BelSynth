package com.ssrlab.assistant.ui.choose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityChooseBinding
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.PREFERENCES
import com.ssrlab.assistant.utils.THEME
import com.ssrlab.assistant.utils.helpers.LaunchToolbarAnimHelper
import java.util.*

class ChooseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseBinding
    private lateinit var animHelper: LaunchToolbarAnimHelper

    private val mainApp = MainApplication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp.setContext(this@ChooseActivity)

        loadPreferences()

        animHelper = LaunchToolbarAnimHelper()
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val locale = sharedPreferences.getString(LOCALE, "be")
        val nightMode = sharedPreferences.getBoolean(THEME, false)

        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

        if (nightMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        mainApp.setTheme(nightMode)

        val config = mainApp.getContext().resources.configuration
        config.setLocale(locale?.let { Locale(it) })
        locale?.let { Locale(it) }?.let { Locale.setDefault(it) }

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)
        mainApp.setLocale(locale!!)
    }

    fun savePreferences(locale: String) {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(LOCALE, locale)
            apply()
        }

        recreate()
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

    fun intentToChat(chatId: String, title: String, img: Int, role_code: String = "assistant", roleInt: Int = 0, role: String = "") {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chat_id", chatId)
        intent.putExtra("chat_name", title)
        intent.putExtra("chat_img", img)
        intent.putExtra("chat_role_code", role_code)
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
}