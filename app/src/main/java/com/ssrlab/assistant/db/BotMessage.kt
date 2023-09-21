package com.ssrlab.assistant.db

data class BotMessage(
    val id: Int = 0,
    val text: String = "",
    val audioLink: String = ""
)
