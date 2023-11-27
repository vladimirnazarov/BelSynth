package com.ssrlab.assistant.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.main.ChooseActivity
import com.ssrlab.assistant.utils.FIRST_LAUNCH
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.PREFERENCES
import com.ssrlab.assistant.utils.THEME
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private val mainApp = MainApplication()
    private lateinit var binding: ActivityLaunchBinding

    private var isFirstLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp.setContext(this@LaunchActivity)
        loadPreferences()

    }

    override fun onStop() {
        super.onStop()

        savePreferences()
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean(THEME, false)
        isFirstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH, true)
        val locale = sharedPreferences.getString(LOCALE, "be")

        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

        if (nightMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        mainApp.setTheme(nightMode)

        val config = mainApp.getContext().resources.configuration
        config.setLocale(locale?.let { Locale(it) })
        locale?.let { Locale(it) }?.let { Locale.setDefault(it) }

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)
        mainApp.setLocale(locale!!)
    }

    private fun savePreferences() {
        if (isFirstLaunch) {
            val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
            sharedPreferences.edit {
                putBoolean(FIRST_LAUNCH, false)
                apply()
            }
        }
    }

    fun intentNext() {
        val intent = Intent(this@LaunchActivity, ChooseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun isFirstLaunch() = isFirstLaunch

    fun getMainApp() = mainApp
}