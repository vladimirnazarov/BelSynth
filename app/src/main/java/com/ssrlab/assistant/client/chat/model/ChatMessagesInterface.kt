package com.ssrlab.assistant.client.chat.model

import com.ssrlab.assistant.db.objects.messages.Message

interface ChatMessagesInterface {

    fun loadMessages(chatId: String, onSuccess: (ArrayList<Message>) -> Unit, onFailure: (String) -> Unit)
}