package com.ssrlab.assistant.ui.chat

import android.os.Bundle
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.databinding.ActivityChatBinding

class ChatActivityNew: BaseActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}