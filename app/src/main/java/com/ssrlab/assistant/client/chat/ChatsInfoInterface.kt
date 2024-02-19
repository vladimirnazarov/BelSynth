package com.ssrlab.assistant.client.chat

import com.ssrlab.assistant.db.objects.info.ChatInfoObject

interface ChatsInfoInterface {

    fun getAllChats(onSuccess: (ArrayList<ChatInfoObject>) -> Unit, onFailure: (String) -> Unit)

    fun createChat(name: String, role: String, botName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit)
    fun editChat(name: String, role: String, botName: String, chatId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit)
    fun deleteChat(chatId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit)
}