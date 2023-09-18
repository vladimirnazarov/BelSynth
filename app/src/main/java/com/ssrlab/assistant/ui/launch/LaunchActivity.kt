package com.ssrlab.assistant.ui.launch

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.chat.MainActivity

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun setUpToolbar(title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        binding.apply {
            launchToolbarTitle.text = title

            launchToolbarBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            if (navController != null) {
                launchToolbarSettings.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_settingsFragment) }
                launchToolbarContacts.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_contactsFragment) }
            }

            if (isBackButtonVisible) launchToolbarBack.visibility = View.VISIBLE
            else launchToolbarBack.visibility = View.INVISIBLE

            if (isAdditionalButtonsVisible) {
                launchToolbarContacts.visibility = View.VISIBLE
                launchToolbarSettings.visibility = View.VISIBLE
            } else {
                launchToolbarContacts.visibility = View.GONE
                launchToolbarSettings.visibility = View.INVISIBLE
            }
        }
    }

    fun intentToChat(chatId: Int, title: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("chat_id", chatId)
        intent.putExtra("chat_name", title)
        startActivity(intent)
    }
}