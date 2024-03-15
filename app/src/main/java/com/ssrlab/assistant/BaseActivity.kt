package com.ssrlab.assistant

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.PREFERENCES
import java.util.Locale

open class BaseActivity: AppCompatActivity() {

    val mainApp = MainApplication()
    lateinit var sharedPreferences: SharedPreferences

    private fun Context.loadPreferences(): Context {
        sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val localeString = sharedPreferences.getString(LOCALE, "be").toString()

        val locale = Locale(localeString)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return createConfigurationContext(config)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ContextWrapper(newBase?.loadPreferences()))
    }

    fun showErrorSnack(errorMessage: String, view: View) {
        val snack = Snackbar.make(view, errorMessage, Snackbar.LENGTH_SHORT)
        snack.apply {
            setTextColor(ContextCompat.getColor(context, R.color.snack_text))
            setBackgroundTint(ContextCompat.getColor(context, R.color.error))
            show()
        }
    }
}