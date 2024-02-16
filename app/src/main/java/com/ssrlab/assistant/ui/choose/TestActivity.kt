package com.ssrlab.assistant.ui.choose

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssrlab.assistant.client.chat.ChatMessagesClient
import com.ssrlab.assistant.client.chat.ChatsInfoClient
import com.ssrlab.assistant.client.chat.MessageClient
import com.ssrlab.assistant.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var chatsInfoClient: ChatsInfoClient
    private lateinit var chatMessagesClient: ChatMessagesClient
    private lateinit var messageClient: MessageClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        initClients()
        initChatInfoAction()
    }

    private fun initClients() {
        chatsInfoClient = ChatsInfoClient(this@TestActivity)
        chatMessagesClient = ChatMessagesClient()
        messageClient = MessageClient
    }

    private fun initChatInfoAction() {
        binding.testChatsInfo.setOnClickListener {
            chatsInfoClient.getAllChats({ infoArray ->
                runOnUiThread { binding.testDebug.text = infoArray.toString() }
            }, { errorMessage ->
                runOnUiThread { binding.testDebug.text = errorMessage }
            })
        }
    }
}