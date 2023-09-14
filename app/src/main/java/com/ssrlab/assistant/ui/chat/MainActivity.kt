package com.ssrlab.assistant.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssrlab.assistant.databinding.ActivityMainBinding
import com.ssrlab.assistant.utils.ChatAnimHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var animHelper: ChatAnimHelper

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animHelper = ChatAnimHelper()

        setUpToolbar()
    }

    override fun onResume() {
        super.onResume()

        animHelper.apply {
            loadDotsAnim(this@MainActivity, binding, scope)
            loadRecordAnim(this@MainActivity, binding)
        }
    }

    private fun setUpToolbar() {
        binding.apply {
            mainToolbarBack.setOnClickListener { goBack() }

            mainToolbarTitle.text = intent.getStringExtra("chat_name")
        }
    }

    private fun goBack() { onBackPressedDispatcher.onBackPressed() }
}