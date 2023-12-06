package com.ssrlab.assistant.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.main.ChooseActivity
import com.ssrlab.assistant.utils.PREFERENCES
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private val mainApp = MainApplication()
    private lateinit var binding: ActivityLaunchBinding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        mainApp.setContext(this@LaunchActivity)
        mainApp.loadPreferences(sharedPreferences)

        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStop() {
        super.onStop()

        mainApp.savePreferences(sharedPreferences, this@LaunchActivity)
    }

    fun intentNext() {
        val intent = Intent(this@LaunchActivity, ChooseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun getMainApp() = mainApp
}