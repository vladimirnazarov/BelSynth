package com.ssrlab.assistant.ui.choose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.MessageClient
import com.ssrlab.assistant.client.chat.ChatMessagesClient
import com.ssrlab.assistant.client.chat.ChatsInfoClient
import com.ssrlab.assistant.databinding.ActivityTestBinding
import com.ssrlab.assistant.utils.NULL
import java.io.File

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var chatsInfoClient: ChatsInfoClient
    private lateinit var chatMessagesClient: ChatMessagesClient
    private lateinit var messageClient: MessageClient

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val observableFile = MutableLiveData<File?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerActivityResultAction()
        setUpObserver()
    }

    override fun onStart() {
        super.onStart()

        initClients()
        initChatInfoAction()
        initChatCreateAction()
        initChatEditAction()
        initDeleteChatAction()
        initGetMessagesAction()
        initSendTextMessageAction()
        initLoadAudioAction()
        initSendAudioMessageAction()
    }

    private fun initClients() {
        chatsInfoClient = ChatsInfoClient(this@TestActivity)
        chatMessagesClient = ChatMessagesClient(this@TestActivity)
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
                                    runOnUiThread { testDebug.text = it }
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
                        runOnUiThread { testDebug.text = it }
                    })
                } else {
                    val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                    testDebug.text = errorMessage
                }
            }
        }
    }

    private fun initGetMessagesAction() {
        binding.apply {
            testMessagesButton.setOnClickListener {
                val chatId = testMessagesInputChatId.text ?: ""
                if (chatId.isNotEmpty()) {
                    chatMessagesClient.loadMessages(chatId.toString(), { messages ->
                        runOnUiThread { testDebug.text = messages.toString() }
                    }, {
                        runOnUiThread { testDebug.text = it }
                    })
                } else {
                    val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                    testDebug.text = errorMessage
                }
            }
        }
    }

    private fun initSendTextMessageAction() {
        binding.apply {
            testSendTextButton.setOnClickListener {
                val text = testSendTextInputText.text ?: ""
                val chatId = testSendTextInputChatId.text ?: ""

                if (text.isNotEmpty()) {
                    if (chatId.isNotEmpty()) {
                        chatMessagesClient.sendMessage(chatId.toString(), text.toString(), NULL,  { message ->
                            runOnUiThread { testDebug.text = message.toString() }
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
            }
        }
    }

    private fun initSendAudioMessageAction() {
        binding.apply {
            testSendAudioButton.setOnClickListener {
                val chatId = testSendAudioInputChatId.text ?: ""
                if (chatId.isNotEmpty()) {
                    if (observableFile.value != null) {
                        chatMessagesClient.uploadAudio(observableFile.value!!, { link ->
                            chatMessagesClient.sendMessage(chatId.toString(), NULL, link, { message ->
                                runOnUiThread { testDebug.text = message.toString() }
                            }, { errorMessage ->
                                runOnUiThread { testDebug.text = errorMessage }
                            })
                        }, { errorMessage ->
                            runOnUiThread { testDebug.text = errorMessage }
                        })
                    } else {
                        val errorMessage = ContextCompat.getString(this@TestActivity, R.string.file_isnt_chosen)
                        testDebug.text = errorMessage
                    }
                } else {
                    val errorMessage = ContextCompat.getString(this@TestActivity, R.string.something_went_wrong)
                    testDebug.text = errorMessage
                }
            }
        }
    }

    private fun initLoadAudioAction() {
        binding.testChooseAudioButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            launcher.launch(intent)
        }
    }

    private fun registerActivityResultAction() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val uri = result.data?.data

                    if (uri != null) {
                        val cursor = contentResolver.query(uri, null, null, null, null)
                        if (cursor != null) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()

                            val displayName = cursor.getString(nameIndex)
                            cursor.close()

                            val inputStream = contentResolver.openInputStream(uri)
                            val file = File(cacheDir, displayName)
                            file.outputStream().use {
                                it.write(inputStream?.readBytes())
                                it.close()

                                inputStream?.close()
                                observableFile.value = file
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpObserver() {
        observableFile.observe(this) {
            if (it != null) binding.testChosenTitle.text = it.name
        }
    }
}