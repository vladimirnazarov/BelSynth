package com.ssrlab.assistant.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.ssrlab.assistant.client.IsUserSignedIn
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.ui.choose.ChooseActivity
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.utils.AUTH_EMAIL
import com.ssrlab.assistant.utils.AUTH_PASSWORD
import com.ssrlab.assistant.utils.CHAT_SOUND
import com.ssrlab.assistant.utils.FIRST_LAUNCH
import com.ssrlab.assistant.utils.IS_GOOGLE_SIGN
import com.ssrlab.assistant.utils.IS_USER_RATED
import com.ssrlab.assistant.utils.IS_USER_SIGNED_IN
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.THEME
import java.util.Locale

class MainApplication: Application() {

    private var locale = Locale("en")
    private var appTheme = false

    private var localeString = ""
    private var isFirstLaunch = true
    private var isUserRated = false
    private var isSoundEnabled = true

    private var isUserSignedInObj = IsUserSignedIn()
    private var isUserSignedIn = false
    private var isGoogleSign = false
    private var userEmail = ""
    private var userPassword = ""

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
    fun isUserRated() = isUserRated

    fun loadPreferences(sharedPreferences: SharedPreferences) {
        localeString = sharedPreferences.getString(LOCALE, "be").toString()
        appTheme = sharedPreferences.getBoolean(THEME, false)
        isUserRated = sharedPreferences.getBoolean(IS_USER_RATED, false)
        isFirstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH, true)
        isSoundEnabled = sharedPreferences.getBoolean(CHAT_SOUND, true)

        isUserSignedIn = sharedPreferences.getBoolean(IS_USER_SIGNED_IN, false)
        isGoogleSign = sharedPreferences.getBoolean(IS_GOOGLE_SIGN, false)
        userEmail = sharedPreferences.getString(AUTH_EMAIL, "").toString()
        userPassword = sharedPreferences.getString(AUTH_PASSWORD, "").toString()

        isUserSignedInObj.apply {
            isSignedIn = isUserSignedIn
            isGoogle = isGoogleSign
            email = userEmail
            password = userPassword
        }

        loadTheme()
    }

    private fun loadTheme() {
        if (appTheme) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    fun savePreferences(sharedPreferences: SharedPreferences, activity: Activity, locale: String? = null, value: Boolean? = null) {
        when(activity) {
            is LaunchActivity -> {
                with(sharedPreferences.edit()) {
                    putString(LOCALE, locale)
                    apply()
                }

                activity.recreate()
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

    fun saveTheme(theme: Boolean, sharedPreferences: SharedPreferences, activity: Activity) {
        with(sharedPreferences.edit()) {
            putBoolean(THEME, theme)
            apply()
        }

        activity.recreate()
    }

    fun saveSecondLaunch(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit {
            putBoolean(FIRST_LAUNCH, false)
            apply()
        }
    }

    fun saveIsUserRated(sharedPreferences: SharedPreferences) {
        with (sharedPreferences.edit()) {
            putBoolean(IS_USER_RATED, true)
            apply()
        }
    }

    fun saveUserSignedIn(
        sharedPreferences: SharedPreferences,
        isUserSigned: Boolean = false,
        isGoogle: Boolean = false,
        email: String = "",
        password: String = "",
    ) {
        with (sharedPreferences.edit()) {
            putBoolean(IS_USER_SIGNED_IN, isUserSigned)
            putBoolean(IS_GOOGLE_SIGN, isGoogle)
            putString(AUTH_EMAIL, email)
            putString(AUTH_PASSWORD, password)
            apply()
        }
    }

    fun getIsUserSignObject() = isUserSignedInObj
}