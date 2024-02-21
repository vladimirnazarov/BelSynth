package com.ssrlab.assistant.ui.choose

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import androidx.core.content.ContextCompat
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.chat.ChatMessagesClient
import com.ssrlab.assistant.client.chat.ChatsInfoClient
import com.ssrlab.assistant.client.MessageClient
import com.ssrlab.assistant.databinding.ActivityTestBinding
import com.ssrlab.assistant.utils.MAX_LENGTH
import com.ssrlab.assistant.utils.helpers.TextHelper

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

        setUpEditTexts()

        initClients()
        initChatInfoAction()
        initChatCreateAction()
        initChatEditAction()
        initDeleteChatAction()
    }

    private fun setUpEditTexts() {
        val filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH), TextHelper(this@TestActivity).createNoSpaceFilter())
        binding.apply {
            testCreateInputBot.filters = filters
            testCreateInputRole.filters = filters

            testEditInputBot.filters = filters
            testEditInputChatId.filters = filters
            testEditInputRole.filters = filters

            testDeleteInputChatId.filters = filters
        }
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