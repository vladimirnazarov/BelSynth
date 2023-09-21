package com.ssrlab.assistant.db

data class ChatMessage(
    val id: Int = 0,
    val text: String = "",
    val audio: Boolean = false
)
