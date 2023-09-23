package com.ssrlab.assistant.db

import java.io.File

data class ChatMessage(
    val id: Int = 0,
    val text: String = "",
    val audioSend: File? = null,
    val audioResponse: String = "",
)
