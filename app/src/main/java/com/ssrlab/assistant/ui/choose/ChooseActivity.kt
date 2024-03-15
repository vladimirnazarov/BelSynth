package com.ssrlab.assistant.ui.choose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.client.chat.ChatsInfoClient
import com.ssrlab.assistant.databinding.ActivityChooseBinding
import com.ssrlab.assistant.db.objects.chat.ChatInfoObject
import com.ssrlab.assistant.ui.chat.ChatActivityNew
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.utils.CHAT_ID
import com.ssrlab.assistant.utils.CHAT_IMAGE
import com.ssrlab.assistant.utils.CHAT_NAME
import com.ssrlab.assistant.utils.CHAT_ROLE
import com.ssrlab.assistant.utils.CHAT_ROLE_CODE
import com.ssrlab.assistant.utils.CHAT_ROLE_INT
import com.ssrlab.assistant.utils.CHAT_SPEAKER
import com.ssrlab.assistant.utils.NULL
import com.ssrlab.assistant.utils.helpers.LaunchToolbarAnimHelper

class ChooseActivity : BaseActivity() {

    private lateinit var binding: ActivityChooseBinding
    private lateinit var animHelper: LaunchToolbarAnimHelper
    private lateinit var chatsInfoClient: ChatsInfoClient

    private var chatsInfoArray = arrayListOf<ChatInfoObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        mainApp.setContext(this@ChooseActivity)
        mainApp.loadPreferences(sharedPreferences)
        super.onCreate(savedInstanceState)

        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animHelper = LaunchToolbarAnimHelper()
        chatsInfoClient = ChatsInfoClient(this@ChooseActivity)
    }

    override fun onStart() {
        super.onStart()

        getAllChats()
    }

    fun setUpToolbar(title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        animHelper.setUpToolbar(this@ChooseActivity, binding, title, isBackButtonVisible, isAdditionalButtonsVisible, navController)
    }

    private fun getAllChats() {
        chatsInfoClient.getAllChats({
            chatsInfoArray = it
        }, {
            showErrorSnack(it, binding.root)
        })
    }

    fun intentToChat(speaker: String, title: String, img: Int, roleCode: String = "assistant", roleInt: Int = 0, role: String = "") {
        var chatId = NULL
        for (i in chatsInfoArray) {
            if (i.botName == speaker && i.role == roleCode) {
                chatId = i.chatId
            }
        }

        if (chatId == NULL) {
            chatsInfoClient.createChat("${title}_$roleCode", roleCode, speaker, {
                chatId = it
                intentToChat(chatId, speaker, title, img, roleCode, roleInt, role)
            }, {
                showErrorSnack(it, binding.root)
            })
        } else {
            intentToChat(chatId, speaker, title, img, roleCode, roleInt, role)
        }
    }

    /**
     * According to the previous fun
     */
    private fun intentToChat(chatId: String, speaker: String, title: String, img: Int, roleCode: String = "assistant", roleInt: Int = 0, role: String = "") {
        val intent = Intent(this, ChatActivityNew::class.java)
        intent.apply {
            putExtra(CHAT_SPEAKER, speaker)
            putExtra(CHAT_NAME, title)
            putExtra(CHAT_IMAGE, img)
            putExtra(CHAT_ROLE_CODE, roleCode)
            putExtra(CHAT_ROLE_INT, roleInt)
            putExtra(CHAT_ROLE, role)
            putExtra(CHAT_ID, chatId)

            runOnUiThread {
                startActivity(this@apply)
            }
        }
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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