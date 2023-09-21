package com.ssrlab.assistant.db

import java.io.File

data class UserVoiceMessage(
    val id: Int = 0,
    val audio: File? = null
)
