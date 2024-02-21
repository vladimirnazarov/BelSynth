package com.ssrlab.assistant.db.objects.messages

data class Message(
    val text: String = "null",
    val role: String,
    val audio: String = "null"
)
