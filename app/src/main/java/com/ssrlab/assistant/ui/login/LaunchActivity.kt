package com.ssrlab.assistant.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.databinding.ActivityLaunchBinding

@SuppressLint("CustomSplashScreen")
class LaunchActivity : BaseActivity() {

    private lateinit var binding: ActivityLaunchBinding

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var googleCallback: (ActivityResult) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        mainApp.setContext(this@LaunchActivity)
        mainApp.loadPreferences(sharedPreferences)
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) googleCallback(result)
        }
    }

    override fun onStart() {
        super.onStart()

        mainApp.saveSecondLaunch(sharedPreferences)
    }

    fun googleIntent(callback: (ActivityResult) -> Unit) {
        googleCallback = { callback(it) }
    }

    fun getLauncher() = launcher
}