package com.ssrlab.assistant.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.ui.main.ChooseActivity
import com.ssrlab.assistant.utils.CHAT_SOUND
import com.ssrlab.assistant.utils.FIRST_LAUNCH
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.THEME
import java.util.Locale

class MainApplication: Application() {

    private var locale = Locale("en")
    private val config = Configuration()
    private var theme = false

    private var localeString = ""
    private var isFirstLaunch = true
    private var isSoundEnabled = true

    override fun onCreate() {
        super.onCreate()

        Locale.setDefault(locale)
    }

    private lateinit var context: Context

    fun getContext() = context
    fun setContext(context: Context){
        this.context = context
    }

    fun getLocale() = localeString

    fun isFirstLaunch() = isFirstLaunch
    fun isSoundEnabled() = isSoundEnabled

    @Suppress("DEPRECATION")
    fun loadPreferences(sharedPreferences: SharedPreferences) {
        localeString = sharedPreferences.getString(LOCALE, "be").toString()
        theme = sharedPreferences.getBoolean(THEME, false)
        isFirstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH, true)
        isSoundEnabled = sharedPreferences.getBoolean(CHAT_SOUND, true)

        if (theme) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        config.setLocale(locale)
        context.resources.configuration.setLocale(locale)

        val config = context.resources.configuration
        config.setLocale(Locale(localeString))
        Locale.setDefault(Locale(localeString))

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun savePreferences(sharedPreferences: SharedPreferences, activity: Activity, locale: String? = null, value: Boolean? = null) {
        when(activity) {
            is LaunchActivity -> {
                if (isFirstLaunch) {
                    sharedPreferences.edit {
                        putBoolean(FIRST_LAUNCH, false)
                        apply()
                    }
                }
            }
            is ChooseActivity -> {
                with(sharedPreferences.edit()) {
                    putString(LOCALE, locale)
                    apply()
                }

                activity.recreate()
            }
            is ChatActivity -> {
                sharedPreferences.edit {
                    if (value != null) putBoolean(CHAT_SOUND, value)
                    apply()
                }
            }
        }
    }
}