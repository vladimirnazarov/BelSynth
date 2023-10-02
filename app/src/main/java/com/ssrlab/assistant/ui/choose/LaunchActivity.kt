package com.ssrlab.assistant.ui.choose

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.chat.MainActivity
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.PREFERENCES
import com.ssrlab.assistant.utils.THEME
import com.ssrlab.assistant.utils.helpers.LaunchToolbarAnimHelper
import java.util.*

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var animHelper: LaunchToolbarAnimHelper

    private val mainApp = MainApplication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp.setContext(this@LaunchActivity)

        loadPreferences()

        animHelper = LaunchToolbarAnimHelper()
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val locale = sharedPreferences.getString(LOCALE, "en")
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
        animHelper.setUpToolbar(this@LaunchActivity, binding, title, isBackButtonVisible, isAdditionalButtonsVisible, navController)
    }

    fun intentToChat(chatId: String, title: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("chat_id", chatId)
        intent.putExtra("chat_name", title)
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