package com.ssrlab.assistant.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssrlab.assistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun goBack() { onBackPressedDispatcher.onBackPressed() }
}