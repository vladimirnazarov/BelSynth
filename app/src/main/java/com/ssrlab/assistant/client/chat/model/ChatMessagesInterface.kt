package com.ssrlab.assistant.client.chat.model

import com.ssrlab.assistant.db.objects.messages.Message
import java.io.File

interface ChatMessagesInterface {

    fun loadMessages(chatId: String, onSuccess: (ArrayList<Message>) -> Unit, onFailure: (String) -> Unit)

    fun sendAudio(audioFile: File, onSuccess: (String) -> Unit, onFailure: (String) -> Unit)

    fun sendMessage(
        chatId: String,
        text: String? = null,
        audioLink: String? = null,
        voiceType: String,
        role: String = "assistant",
        onSuccess: (Message) -> Unit,
        onFailure: (String) -> Unit
    )
}