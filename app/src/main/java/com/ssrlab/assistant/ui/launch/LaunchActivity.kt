package com.ssrlab.assistant.ui.launch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.chat.MainActivity
import com.ssrlab.assistant.utils.helpers.LaunchToolbarAnimHelper

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var animHelper: LaunchToolbarAnimHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animHelper = LaunchToolbarAnimHelper()
    }

    fun setUpToolbar(title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        animHelper.setUpToolbar(this@LaunchActivity, binding, title, isBackButtonVisible, isAdditionalButtonsVisible, navController)
    }

    fun intentToChat(chatId: Int, title: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("chat_id", chatId)
        intent.putExtra("chat_name", title)
        startActivity(intent)
    }
}