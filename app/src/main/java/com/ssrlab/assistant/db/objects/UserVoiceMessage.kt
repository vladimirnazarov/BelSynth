package com.ssrlab.assistant.db.objects

import java.io.File

data class UserVoiceMessage(
    val id: Int = 0,
    val audio: File? = null
)
