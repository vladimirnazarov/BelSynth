package com.ssrlab.assistant.db.objects.chat

data class ChatInfoObject(
    val id: Int,
    val chatId: String,
    val name: String,
    val botName: String,
    val role: String
)
