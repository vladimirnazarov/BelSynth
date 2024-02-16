package com.ssrlab.assistant.ui.choose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.databinding.ActivityChooseBinding
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.utils.helpers.LaunchToolbarAnimHelper

class ChooseActivity : BaseActivity() {

    private lateinit var binding: ActivityChooseBinding
    private lateinit var animHelper: LaunchToolbarAnimHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        mainApp.setContext(this@ChooseActivity)
        mainApp.loadPreferences(sharedPreferences)
        super.onCreate(savedInstanceState)

        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animHelper = LaunchToolbarAnimHelper()
    }

    fun setUpToolbar(title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        animHelper.setUpToolbar(this@ChooseActivity, binding, title, isBackButtonVisible, isAdditionalButtonsVisible, navController)
    }

    fun intentToChat(chatId: String, title: String, img: Int, roleCode: String = "assistant", roleInt: Int = 0, role: String = "") {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chat_id", chatId)
        intent.putExtra("chat_name", title)
        intent.putExtra("chat_img", img)
        intent.putExtra("chat_role_code", roleCode)
        intent.putExtra("chat_role_int", roleInt)
        intent.putExtra("chat_role", role)
        startActivity(intent)
    }

    fun intentToPhone(number: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    fun intentToMail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:hello@asistent.by")
        startActivity(intent)
    }

    fun intentBack() {
        val intent = Intent(this@ChooseActivity, LaunchActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun intentToLink(link: String) {
        val webPage = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        startActivity(intent)
    }

    fun intentToTest() {
        val intent = Intent(this@ChooseActivity, TestActivity::class.java)
        startActivity(intent)
    }
}