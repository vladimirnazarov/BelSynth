package com.ssrlab.assistant.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ssrlab.assistant.R
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.choose.ChooseActivity
import com.ssrlab.assistant.utils.FIRST_LAUNCH
import com.ssrlab.assistant.utils.LOCALE
import com.ssrlab.assistant.utils.PREFERENCES
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private val mainApp = MainApplication()

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var controller: WindowInsetsControllerCompat

    private var isFirstLaunch = true

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp.setContext(this@LaunchActivity)
        loadPreferences()

        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onResume() {
        super.onResume()

        controller.hide(WindowInsetsCompat.Type.systemBars())
        initLogo()
    }

    override fun onStop() {
        super.onStop()

        savePreferences()
        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        isFirstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH, true)
        val locale = sharedPreferences.getString(LOCALE, "be")

        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

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

    private fun intentNext() {
        val intent = Intent(this@LaunchActivity, ChooseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun initLogo() {
        if (isFirstLaunch) {
            binding.apply {

                scope.launch {
                    runOnUiThread {
                        launchIc.startAnimation(AnimationUtils.loadAnimation(this@LaunchActivity, R.anim.long_alpha_in))
                        launchIc.visibility = View.VISIBLE
                    }

                    delay(750)

                    runOnUiThread {
                        launchTitle.text = resources.getText(R.string.launch_name)
                        launchTitle.startAnimation(AnimationUtils.loadAnimation(this@LaunchActivity, R.anim.long_alpha_in))
                        launchTitle.visibility = View.VISIBLE
                    }
                }

                val videoPath = "android.resource://$packageName/${R.raw.back}"
                val uri = Uri.parse(videoPath)

                launchVideo.visibility = View.VISIBLE
                launchVideo.setVideoURI(uri)
                launchVideo.setOnPreparedListener {
                    it.playbackParams = it.playbackParams.setSpeed(2.0f)
                    launchVideo.start()
                }
                launchVideo.setOnCompletionListener { intentNext() }
            }
        } else {
            binding.apply {
                launchBg.visibility = View.VISIBLE

                scope.launch {
                    runOnUiThread {
                        launchIc.startAnimation(AnimationUtils.loadAnimation(this@LaunchActivity, R.anim.long_alpha_in))
                        launchIc.visibility = View.VISIBLE
                    }

                    delay(750)

                    runOnUiThread {
                        launchTitle.text = resources.getText(R.string.launch_name)
                        launchTitle.startAnimation(AnimationUtils.loadAnimation(this@LaunchActivity, R.anim.long_alpha_in))
                        launchTitle.visibility = View.VISIBLE
                    }
                }

                scope.launch {
                    delay(3000)
                    runOnUiThread { intentNext() }
                }
            }
        }
    }
}