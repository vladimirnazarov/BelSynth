package com.ssrlab.assistant.db

import java.io.File

data class BotMessage(
    val id: Int = 0,
    val text: String = "",
    val audio: File? = null
)
