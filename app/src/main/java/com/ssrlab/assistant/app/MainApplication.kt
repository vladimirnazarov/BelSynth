package com.ssrlab.assistant.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class MainApplication: Application() {

    private var locale = Locale("en")
    private val config = Configuration()
    private var theme = false

    private var localeString = ""

    override fun onCreate() {
        super.onCreate()

        Locale.setDefault(locale)
    }

    fun setLocale(locale: Locale) {
        this.locale = locale
        config.setLocale(locale)
        context.resources.configuration.setLocale(locale)
    }

    fun setTheme(value: Boolean) { this.theme = value }

    private lateinit var context: Context

    fun getContext() = context
    fun setContext(context: Context){
        this.context = context
    }

    fun setLocale(locale: String) { localeString = locale }
    fun getLocale() = localeString
}