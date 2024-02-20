package com.ssrlab.assistant.ui.choose

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.ssrlab.assistant.R
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
        initChatCreateAction()
        initChatEditAction()
        initDeleteChatAction()
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

    private fun initChatCreateAction() {
        binding.apply {
            testCreateButton.setOnClickListener {
                val chatName = testCreateInputName.text ?: ""
                val chatRole = testCreateInputRole.text ?: ""
                val botName = testCreateInputBot.text ?: ""
                if (chatName.isNotEmpty()) {
                    if (chatRole.isNotEmpty()) {
                        if (botName.isNotEmpty()) {
                            chatsInfoClient.createChat(chatName.toString(), chatRole.toString(), botName.toString(), {
                                    runOnUiThread { testDebug.text = it }
                                }, { errorMessage ->
                                    runOnUiThread { testDebug.text = errorMessage }
                                })
                        } else {
                            val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                            testDebug.text = errorMessage
                        }
                    } else {
                        val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                        testDebug.text = errorMessage
                    }
                } else {
                    val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                    testDebug.text = errorMessage
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initChatEditAction() {
        binding.apply {
            testEditButton.setOnClickListener {
                val chatName = testEditInputName.text ?: ""
                val chatRole = testEditInputRole.text ?: ""
                val botName = testEditInputBot.text ?: ""
                val chatId = testEditInputChatId.text ?: ""
                if (chatName.isNotEmpty()) {
                    if (chatRole.isNotEmpty()) {
                        if (botName.isNotEmpty()) {
                            if (chatId.isNotEmpty()) {
                                chatsInfoClient.editChat(chatName.toString(), chatRole.toString(), botName.toString(), chatId.toString(), {
                                    runOnUiThread { testDebug.text = "Success" }
                                }, {
                                    runOnUiThread {
                                        val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                                        testDebug.text = errorMessage
                                    }
                                })
                            } else {
                                val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                                testDebug.text = errorMessage
                            }
                        } else {
                            val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                            testDebug.text = errorMessage
                        }
                    } else {
                        val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                        testDebug.text = errorMessage
                    }
                } else {
                    val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                    testDebug.text = errorMessage
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initDeleteChatAction() {
        binding.apply {
            testDeleteButton.setOnClickListener {
                val chatId = testDeleteInputChatId.text ?: ""
                if (chatId.isNotEmpty()) {
                    chatsInfoClient.deleteChat(chatId.toString(), {
                        runOnUiThread { testDebug.text = "Success" }
                    }, {
                        runOnUiThread {
                            val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                            testDebug.text = errorMessage
                        }
                    })
                } else {
                    val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                    testDebug.text = errorMessage
                }
            }
        }
    }
}