package com.ssrlab.assistant.db.objects.messages

import com.ssrlab.assistant.utils.NULL

data class Message(
    var text: String = NULL,
    val role: String,
    val audio: String = NULL,
    val duration: Int = 0
)